-- LookForBest 用户行为日志表
-- Flyway V3 | 2026-03-11

CREATE TABLE IF NOT EXISTS user_action_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT COMMENT '用户ID，未登录为NULL',
    session_id  VARCHAR(100) NOT NULL COMMENT '前端会话ID',
    action_type ENUM('view','favorite','compare','search','click','share') NOT NULL COMMENT '行为类型',
    target_type ENUM('robot','category','manufacturer','search') NOT NULL COMMENT '目标类型',
    target_id   BIGINT COMMENT '目标资源ID（robot/category/manufacturer）',
    target_slug VARCHAR(300) COMMENT '目标资源slug',
    extra_data  JSON COMMENT '扩展数据（搜索词、来源等）',
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_action_type (action_type),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at),
    INDEX idx_session (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_date_action (created_at, action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志';
