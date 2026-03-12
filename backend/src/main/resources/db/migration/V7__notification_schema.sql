-- 站内通知表
CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    type        VARCHAR(50)   NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    content     TEXT,
    is_read     TINYINT(1)    NOT NULL DEFAULT 0,
    related_id  BIGINT,
    related_type VARCHAR(50),
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id_read (user_id, is_read),
    INDEX idx_user_id_created (user_id, created_at),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
