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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Rate limiter using Redis sliding window.
 * - 未登录：按 IP 限流，默认 30 req/min
 * - 已登录：IP + userId 双重限流，多账号无法叠加配额
 * - 注册接口：按 IP 严格限流 5 req/min，防止批量注册
 * - 管理员（ROLE_ADMIN / ROLE_SUPERADMIN）不受限流限制
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ClientIpResolver ipResolver;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.default-limit:30}")
    private int defaultLimit;

    @Value("${app.rate-limit.auth-limit:20}")
    private int authLimit;

    @Value("${app.rate-limit.user-limit:20}")
    private int userLimit;

    @Value("${app.rate-limit.register-limit:5}")
    private int registerLimit;

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

        // 管理员不受限流限制
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAdmin(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = ipResolver.resolve(request);
        long window = System.currentTimeMillis() / (windowSeconds * 1000L);

        // 注册接口：按 IP 单独限流（最严格）
        if (uri.equals("/api/v1/auth/register")) {
            enforce(response, "rate:reg:" + ip + ":" + window, registerLimit, ip, uri);
            if (response.isCommitted()) return;
            filterChain.doFilter(request, response);
            return;
        }

        // 其他 auth 接口（登录等）：按 IP 限流
        if (uri.startsWith("/api/v1/auth/")) {
            enforce(response, "rate:auth:" + ip + ":" + window, authLimit, ip, uri);
            if (response.isCommitted()) return;
            filterChain.doFilter(request, response);
            return;
        }

        // 已登录用户：IP + userId 双重限流，换账号换不了 IP 配额
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            enforce(response, "rate:ip:" + ip + ":" + window, defaultLimit, ip, uri);
            if (response.isCommitted()) return;
            enforce(response, "rate:user:" + auth.getName() + ":" + window, userLimit, ip, uri);
            if (response.isCommitted()) return;
        } else {
            enforce(response, "rate:ip:" + ip + ":" + window, defaultLimit, ip, uri);
            if (response.isCommitted()) return;
        }

        filterChain.doFilter(request, response);
    }

    private void enforce(HttpServletResponse response, String key, int limit,
                         String ip, String uri) throws IOException {
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
            }
        } catch (Exception e) {
            log.error("Rate limit check failed (Redis error), allowing request: {}", e.getMessage());
        }
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }
}
