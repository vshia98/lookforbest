package com.lookforbest.repository;

import com.lookforbest.entity.UserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {

    /** 统计指定时间范围内总行为数 */
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    /** 按行为类型分组统计 */
    @Query("SELECT l.actionType AS actionType, COUNT(l) AS count " +
           "FROM UserActionLog l " +
           "WHERE l.createdAt >= :from AND l.createdAt <= :to " +
           "GROUP BY l.actionType ORDER BY count DESC")
    List<Map<String, Object>> countByActionTypeInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** 按日期分组统计每日行为数（近N天） */
    @Query(value = "SELECT DATE(created_at) AS date, COUNT(*) AS count " +
                   "FROM user_action_logs " +
                   "WHERE created_at >= :from " +
                   "GROUP BY DATE(created_at) ORDER BY date ASC",
           nativeQuery = true)
    List<Map<String, Object>> countByDaySince(@Param("from") LocalDateTime from);

    /** 统计热门机器人（按 view 行为数量排序） */
    @Query(value = "SELECT l.target_id AS robotId, r.name AS robotName, r.slug AS robotSlug, " +
                   "COUNT(*) AS viewCount " +
                   "FROM user_action_logs l " +
                   "JOIN robots r ON r.id = l.target_id " +
                   "WHERE l.target_type = 'robot' AND l.action_type = 'view' " +
                   "AND l.created_at >= :from " +
                   "GROUP BY l.target_id, r.name, r.slug " +
                   "ORDER BY viewCount DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Map<String, Object>> findTopRobotsByViews(
            @Param("from") LocalDateTime from,
            @Param("limit") int limit);

    /** 统计热门搜索词 */
    @Query(value = "SELECT JSON_UNQUOTE(JSON_EXTRACT(extra_data, '$.keyword')) AS keyword, COUNT(*) AS count " +
                   "FROM user_action_logs " +
                   "WHERE action_type = 'search' AND extra_data IS NOT NULL " +
                   "AND created_at >= :from " +
                   "GROUP BY keyword " +
                   "HAVING keyword IS NOT NULL " +
                   "ORDER BY count DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Map<String, Object>> findTopSearchKeywords(
            @Param("from") LocalDateTime from,
            @Param("limit") int limit);

    /** 每小时分布（当天） */
    @Query(value = "SELECT HOUR(created_at) AS hour, COUNT(*) AS count " +
                   "FROM user_action_logs " +
                   "WHERE DATE(created_at) = CURDATE() " +
                   "GROUP BY HOUR(created_at) ORDER BY hour ASC",
           nativeQuery = true)
    List<Map<String, Object>> countByHourToday();

    /** 活跃用户数（有 user_id 的去重） */
    @Query("SELECT COUNT(DISTINCT l.userId) FROM UserActionLog l " +
           "WHERE l.userId IS NOT NULL AND l.createdAt >= :from AND l.createdAt <= :to")
    long countDistinctActiveUsers(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** 活跃会话数（去重 session_id） */
    @Query("SELECT COUNT(DISTINCT l.sessionId) FROM UserActionLog l " +
           "WHERE l.createdAt >= :from AND l.createdAt <= :to")
    long countDistinctSessions(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
