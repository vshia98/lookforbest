-- 询价表
CREATE TABLE inquiries (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT        NOT NULL,
    robot_id           BIGINT        NOT NULL,
    manufacturer_id    BIGINT        NOT NULL,
    message            TEXT          NOT NULL,
    contact_name       VARCHAR(100),
    contact_email      VARCHAR(200)  NOT NULL,
    contact_phone      VARCHAR(50),
    contact_company    VARCHAR(200),
    status             VARCHAR(20)   NOT NULL DEFAULT 'pending',
    replied_at         DATETIME,
    reply_content      TEXT,
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_robot_id (robot_id),
    INDEX idx_manufacturer_id (manufacturer_id),
    INDEX idx_status (status),
    CONSTRAINT fk_inquiry_user         FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE CASCADE,
    CONSTRAINT fk_inquiry_robot        FOREIGN KEY (robot_id)        REFERENCES robots(id)        ON DELETE CASCADE,
    CONSTRAINT fk_inquiry_manufacturer FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id) ON DELETE CASCADE
);
