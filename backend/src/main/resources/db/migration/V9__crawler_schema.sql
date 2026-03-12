-- 爬虫数据源配置表
CREATE TABLE IF NOT EXISTS crawler_sources (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '数据源名称',
    description     TEXT COMMENT '描述',
    url_pattern     VARCHAR(500) NOT NULL COMMENT '起始 URL（支持列表，逗号分隔）',
    config          JSON NOT NULL COMMENT '爬取规则配置（选择器、字段映射等）',
    is_active       TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    cron_expr       VARCHAR(100) DEFAULT '0 0 3 * * ?' COMMENT 'Cron 表达式（默认每天凌晨3点）',
    max_pages       INT NOT NULL DEFAULT 10 COMMENT '最大爬取页数',
    delay_ms        INT NOT NULL DEFAULT 1000 COMMENT '请求间隔（毫秒）',
    last_crawl_at   DATETIME COMMENT '上次爬取时间',
    last_crawl_status VARCHAR(20) COMMENT '上次爬取状态: success/failed/running',
    total_crawled   BIGINT NOT NULL DEFAULT 0 COMMENT '累计爬取记录数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫数据源配置';

-- 爬取运行日志表
CREATE TABLE IF NOT EXISTS crawler_run_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id       BIGINT NOT NULL COMMENT '数据源 ID',
    started_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    finished_at     DATETIME COMMENT '结束时间',
    status          VARCHAR(20) NOT NULL DEFAULT 'running' COMMENT 'running/success/failed/cancelled',
    pages_crawled   INT NOT NULL DEFAULT 0 COMMENT '爬取页数',
    items_crawled   INT NOT NULL DEFAULT 0 COMMENT '采集条数',
    items_upserted  INT NOT NULL DEFAULT 0 COMMENT '写入/更新条数',
    items_skipped   INT NOT NULL DEFAULT 0 COMMENT '跳过（内容未变）条数',
    error_msg       TEXT COMMENT '错误信息',
    triggered_by    VARCHAR(50) DEFAULT 'scheduler' COMMENT 'scheduler/manual',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source_id (source_id),
    INDEX idx_started_at (started_at),
    FOREIGN KEY (source_id) REFERENCES crawler_sources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬取运行日志';

-- 增量爬取内容哈希表（用于检测变化，跳过未变化内容）
CREATE TABLE IF NOT EXISTS crawler_item_hashes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id       BIGINT NOT NULL COMMENT '数据源 ID',
    item_url        VARCHAR(1000) NOT NULL COMMENT '条目 URL',
    content_hash    VARCHAR(64) NOT NULL COMMENT '内容 MD5/SHA256',
    robot_id        BIGINT COMMENT '关联的机器人 ID（如已写入）',
    last_crawled_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上次爬取时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source_url (source_id, item_url(500)),
    INDEX idx_source_id (source_id),
    FOREIGN KEY (source_id) REFERENCES crawler_sources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='增量爬取哈希';
