-- 机器人评测表
CREATE TABLE robot_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL COMMENT 'Markdown 内容',
    pros TEXT COMMENT '优点',
    cons TEXT COMMENT '缺点',
    rating TINYINT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
    images JSON COMMENT '图片URL数组',
    status ENUM('draft','pending_review','published','rejected') NOT NULL DEFAULT 'draft',
    reject_reason VARCHAR(500),
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_robot_id (robot_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (robot_id) REFERENCES robots(id)
);

-- 应用案例表
CREATE TABLE case_studies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL COMMENT 'Markdown',
    robot_ids JSON COMMENT '关联机器人ID数组',
    industry VARCHAR(100),
    images JSON,
    status ENUM('draft','pending_review','published','rejected') NOT NULL DEFAULT 'draft',
    reject_reason VARCHAR(500),
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_industry (industry),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 点赞表（评测 + 案例通用）
CREATE TABLE ugc_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type ENUM('review','case_study') NOT NULL,
    target_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 举报表
CREATE TABLE ugc_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type ENUM('review','case_study') NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status ENUM('pending','handled') NOT NULL DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
