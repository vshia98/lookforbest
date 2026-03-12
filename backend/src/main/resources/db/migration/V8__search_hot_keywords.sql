-- 搜索热词统计表
CREATE TABLE IF NOT EXISTS search_hot_keywords (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword     VARCHAR(200) NOT NULL,
    search_count BIGINT NOT NULL DEFAULT 1,
    last_searched_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_keyword (keyword),
    INDEX idx_search_count (search_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索热词统计';
