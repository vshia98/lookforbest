package com.lookforbest.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * IP-based API rate limiter using Redis sliding window.
 * Default: 100 requests per minute per IP, 20 req/min for auth endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.default-limit:100}")
    private int defaultLimit;

    @Value("${app.rate-limit.auth-limit:20}")
    private int authLimit;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();

        // 跳过静态资源和健康检查
        if (uri.startsWith("/actuator") || uri.startsWith("/swagger") || uri.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        boolean isAuthEndpoint = uri.startsWith("/api/v1/auth/");
        int limit = isAuthEndpoint ? authLimit : defaultLimit;

        String key = "rate:" + ip + ":" + (System.currentTimeMillis() / (windowSeconds * 1000L));

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds + 1));
            }
            if (count != null && count > limit) {
                log.warn("Rate limit exceeded: ip={}, uri={}, count={}", ip, uri, count);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"success\":false,\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":" + System.currentTimeMillis() + "}");
                return;
            }
        } catch (Exception e) {
            // Redis 不可用时放行，避免影响正常服务
            log.error("Rate limit check failed (Redis error), allowing request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
