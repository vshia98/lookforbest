-- AI 对话会话与消息表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_key VARCHAR(100) NOT NULL UNIQUE COMMENT '会话唯一标识（前端生成UUID）',
    user_id     BIGINT       NULL COMMENT '关联用户ID（可匿名）',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_key (session_key)
) COMMENT = 'AI 对话会话';

CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT      NOT NULL COMMENT '所属会话ID',
    role       VARCHAR(20) NOT NULL COMMENT 'user / assistant',
    content    TEXT        NOT NULL COMMENT '消息内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) COMMENT = 'AI 对话消息记录';
