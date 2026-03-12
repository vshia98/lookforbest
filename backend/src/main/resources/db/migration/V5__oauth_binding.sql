-- 允许 password_hash 为 NULL（OAuth 用户无密码）
ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NULL;

-- 社交账号绑定表
CREATE TABLE user_oauth_bindings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    provider        VARCHAR(30)  NOT NULL COMMENT 'google / wechat_web / wechat_miniapp',
    provider_user_id VARCHAR(255) NOT NULL COMMENT '第三方平台的用户唯一标识',
    access_token    VARCHAR(1000),
    refresh_token   VARCHAR(1000),
    extra_data      TEXT         COMMENT '额外信息（JSON）',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
