-- 性能优化索引 | Flyway V4 | 2026-03-11
-- 针对高频查询路径补充复合索引，降低慢查询发生率

-- ============================================================
-- robots 表：搜索、过滤、排序场景
-- ============================================================

-- 机器人列表：按分类 + 状态 + 特色排序（高频首页/列表页）
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_category_status_featured
        (category_id, status, is_featured, sort_order, view_count);

-- 机器人列表：按厂商 + 状态
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_manufacturer_status
        (manufacturer_id, status);

-- 机器人列表：按发布年份排序（"最新"排序）
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_release_year
        (release_year DESC, status);

-- 机器人列表：按浏览量排序（"热门"排序）
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_view_count
        (view_count DESC, status);

-- 机器人详情：slug 查找
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_slug (slug);

-- 机器人过滤：payload 范围查询
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_payload (payload_kg);

-- 机器人过滤：reach 范围查询
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_reach (reach_mm);

-- 机器人过滤：自由度
ALTER TABLE robots
    ADD INDEX IF NOT EXISTS idx_robots_dof (dof);

-- ============================================================
-- user_action_logs 表：分析查询
-- ============================================================

-- 用户行为日志：按用户 + 时间查询
ALTER TABLE user_action_logs
    ADD INDEX IF NOT EXISTS idx_action_user_time
        (user_id, created_at DESC);

-- 用户行为日志：按机器人 + 动作类型
ALTER TABLE user_action_logs
    ADD INDEX IF NOT EXISTS idx_action_robot_type
        (robot_id, action_type, created_at DESC);

-- 用户行为日志：数据分析按时间范围扫描
ALTER TABLE user_action_logs
    ADD INDEX IF NOT EXISTS idx_action_created_at (created_at DESC);

-- ============================================================
-- robot_comments 表
-- ============================================================

-- 评论列表：按机器人 + 创建时间
ALTER TABLE robot_comments
    ADD INDEX IF NOT EXISTS idx_comment_robot_time
        (robot_id, created_at DESC);

-- ============================================================
-- favorites 表
-- ============================================================

-- 收藏：用户收藏列表
ALTER TABLE favorites
    ADD INDEX IF NOT EXISTS idx_favorites_user_time
        (user_id, created_at DESC);

-- ============================================================
-- 慢查询配置建议（需在 MySQL 配置或 RDS 参数组中设置）
-- slow_query_log = ON
-- long_query_time = 1
-- log_queries_not_using_indexes = ON
-- ============================================================
