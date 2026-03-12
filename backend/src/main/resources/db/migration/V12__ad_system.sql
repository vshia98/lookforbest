-- 广告位表
CREATE TABLE ad_placements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL COMMENT '广告标题',
    description VARCHAR(500) COMMENT '描述',
    image_url VARCHAR(500) COMMENT '广告图片URL',
    link_url VARCHAR(500) COMMENT '点击跳转URL',
    position ENUM('home_banner','list_promoted','detail_sidebar','search_top') NOT NULL COMMENT '广告位置',
    ad_type ENUM('banner','card','text') NOT NULL DEFAULT 'banner',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（越大越靠前）',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    start_date DATE COMMENT '投放开始日期',
    end_date DATE COMMENT '投放结束日期',
    manufacturer_id BIGINT COMMENT '关联厂商（可选）',
    impression_count BIGINT NOT NULL DEFAULT 0 COMMENT '展示次数',
    click_count BIGINT NOT NULL DEFAULT 0 COMMENT '点击次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_position_active (position, is_active),
    INDEX idx_manufacturer_id (manufacturer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告位';

-- 广告点击日志
CREATE TABLE ad_click_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '用户ID（可选，未登录为NULL）',
    ip VARCHAR(50) COMMENT '访客IP',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    clicked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ad_id (ad_id),
    INDEX idx_clicked_at (clicked_at),
    FOREIGN KEY (ad_id) REFERENCES ad_placements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告点击日志';
