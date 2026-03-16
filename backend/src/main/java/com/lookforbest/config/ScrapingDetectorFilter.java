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
 * 爬虫行为检测 + 自动封禁过滤器。
 *
 * 检测规则（时间窗口内超过阈值即封禁）：
 * 1. 列表接口调用次数超限 —— 典型分页批量下载行为
 * 2. 详情接口调用次数超限 —— 遍历所有机器人详情
 *
 * 封禁后：该 IP 所有 API 请求返回 403，持续 ban-hours 小时。
 * 管理员（ROLE_ADMIN / ROLE_SUPERADMIN）不受检测限制。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScrapingDetectorFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ClientIpResolver ipResolver;

    public static final String BAN_KEY_PREFIX = "ban:ip:";

    @Value("${app.anti-scrape.enabled:true}")
    private boolean enabled;

    /** 时间窗口（分钟），在此窗口内统计请求次数 */
    @Value("${app.anti-scrape.window-minutes:5}")
    private int windowMinutes;

    /** 窗口内列表接口调用上限，超过即封禁 */
    @Value("${app.anti-scrape.list-threshold:10}")
    private int listThreshold;

    /** 窗口内详情接口调用上限，超过即封禁 */
    @Value("${app.anti-scrape.detail-threshold:30}")
    private int detailThreshold;

    /** 封禁时长（小时） */
    @Value("${app.anti-scrape.ban-hours:1}")
    private int banHours;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 只检测 API 接口，跳过静态资源、健康检查
        if (!enabled || !uri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 管理员不受检测限制
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAdmin(auth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = ipResolver.resolve(request);

        // ① 检查是否已被封禁
        String banKey = BAN_KEY_PREFIX + ip;
        String banReason = redisTemplate.opsForValue().get(banKey);
        if (banReason != null) {
            log.debug("Blocked banned IP: ip={}, reason={}", ip, banReason);
            writeForbidden(response);
            return;
        }

        // ② 仅对 GET 请求做爬取行为检测
        if ("GET".equals(request.getMethod())) {
            long window = System.currentTimeMillis() / (windowMinutes * 60_000L);

            if (isListEndpoint(uri)) {
                // 检测翻页行为：记录访问过的最大页码，而不是总调用次数
                // 正常用户很少超过第2-3页，爬虫会系统性地翻到很深的页码
                String pageParam = request.getParameter("page");
                int page = 0;
                try { page = pageParam != null ? Integer.parseInt(pageParam) : 0; } catch (NumberFormatException ignored) {}

                if (page >= listThreshold) {
                    // 直接请求深页码，强烈爬虫信号
                    ban(ip, banKey, "deep pagination scraping (page=" + page + ")");
                    writeForbidden(response);
                    return;
                }

                if (page > 0) {
                    // 记录翻页次数（page > 0 才计，避免正常首页加载被误计）
                    if (exceedsThreshold("scrape:pages:" + ip + ":" + window, listThreshold)) {
                        ban(ip, banKey, "sequential pagination scraping");
                        writeForbidden(response);
                        return;
                    }
                }
            } else if (isDetailEndpoint(uri)) {
                if (exceedsThreshold("scrape:detail:" + ip + ":" + window, detailThreshold)) {
                    ban(ip, banKey, "detail API scraping");
                    writeForbidden(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean exceedsThreshold(String trackKey, int threshold) {
        try {
            Long count = redisTemplate.opsForValue().increment(trackKey);
            if (count != null && count == 1) {
                redisTemplate.expire(trackKey, Duration.ofMinutes(windowMinutes + 1));
            }
            return count != null && count > threshold;
        } catch (Exception e) {
            log.error("Anti-scrape tracking failed (Redis error): {}", e.getMessage());
            return false; // Redis 不可用时放行，避免误伤正常用户
        }
    }

    private void ban(String ip, String banKey, String reason) {
        try {
            redisTemplate.opsForValue().set(banKey, reason, Duration.ofHours(banHours));
            log.warn("Scraping detected, IP banned {}h: ip={}, reason={}", banHours, ip, reason);
        } catch (Exception e) {
            log.error("Failed to set ban key for ip={}: {}", ip, e.getMessage());
        }
    }

    private boolean isListEndpoint(String uri) {
        // /api/v1/robots 或 /api/v1/robots?xxx
        return uri.equals("/api/v1/robots") || uri.equals("/api/v1/robots/");
    }

    private boolean isDetailEndpoint(String uri) {
        // /api/v1/robots/{id} 或 /api/v1/robots/slug/{slug}
        if (!uri.startsWith("/api/v1/robots/")) return false;
        String rest = uri.substring("/api/v1/robots/".length());
        // 排除子资源路径如 /api/v1/robots/123/similar
        return !rest.isEmpty() && !rest.contains("/");
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"code\":403,\"message\":\"您的访问已被限制，请1小时后重试\",\"timestamp\":"
                + System.currentTimeMillis() + "}");
    }
}
