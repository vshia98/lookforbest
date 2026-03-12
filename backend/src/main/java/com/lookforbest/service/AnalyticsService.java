package com.lookforbest.service;

import com.lookforbest.dto.request.TrackActionRequest;
import com.lookforbest.entity.UserActionLog;
import com.lookforbest.repository.UserActionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserActionLogRepository logRepository;

    /** 异步记录一条用户行为日志 */
    @Async
    public void track(TrackActionRequest req, Long userId, HttpServletRequest httpRequest) {
        UserActionLog log = new UserActionLog();
        log.setUserId(userId);
        log.setSessionId(req.getSessionId());
        log.setActionType(req.getActionType());
        log.setTargetType(req.getTargetType());
        log.setTargetId(req.getTargetId());
        log.setTargetSlug(req.getTargetSlug());
        log.setExtraData(req.getExtraData());
        log.setIpAddress(getClientIp(httpRequest));
        String ua = httpRequest.getHeader("User-Agent");
        if (ua != null && ua.length() > 500) ua = ua.substring(0, 500);
        log.setUserAgent(ua);
        logRepository.save(log);
    }

    /** 概览统计（今日、近7天、近30天） */
    public Map<String, Object> getOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime day7Start = now.minusDays(7);
        LocalDateTime day30Start = now.minusDays(30);

        Map<String, Object> result = new HashMap<>();

        // 行为总数
        result.put("totalActions", logRepository.count());
        result.put("todayActions", logRepository.countByCreatedAtBetween(todayStart, now));
        result.put("last7dActions", logRepository.countByCreatedAtBetween(day7Start, now));
        result.put("last30dActions", logRepository.countByCreatedAtBetween(day30Start, now));

        // 活跃用户 & 会话
        result.put("todaySessions", logRepository.countDistinctSessions(todayStart, now));
        result.put("last7dSessions", logRepository.countDistinctSessions(day7Start, now));
        result.put("todayActiveUsers", logRepository.countDistinctActiveUsers(todayStart, now));
        result.put("last7dActiveUsers", logRepository.countDistinctActiveUsers(day7Start, now));

        // 今日行为类型分布
        result.put("todayActionDistribution", logRepository.countByActionTypeInRange(todayStart, now));

        // 近30天行为类型分布
        result.put("last30dActionDistribution", logRepository.countByActionTypeInRange(day30Start, now));

        return result;
    }

    /** 近N天每日行为趋势 */
    public List<Map<String, Object>> getDailyTrends(int days) {
        LocalDateTime from = LocalDate.now().minusDays(days - 1).atStartOfDay();
        return logRepository.countByDaySince(from);
    }

    /** 今日每小时分布 */
    public List<Map<String, Object>> getHourlyToday() {
        return logRepository.countByHourToday();
    }

    /** 热门机器人（按浏览量，近N天） */
    public List<Map<String, Object>> getTopRobots(int days, int limit) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        return logRepository.findTopRobotsByViews(from, limit);
    }

    /** 热门搜索词（近N天） */
    public List<Map<String, Object>> getTopSearchKeywords(int days, int limit) {
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        return logRepository.findTopSearchKeywords(from, limit);
    }

    /** 提取客户端真实IP */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) return ip;
        return request.getRemoteAddr();
    }
}
