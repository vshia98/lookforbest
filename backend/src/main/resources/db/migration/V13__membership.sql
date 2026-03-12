-- 会员套餐表
CREATE TABLE membership_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '套餐名称（如：免费版、专业版、企业版）',
    name_en VARCHAR(100),
    description TEXT,
    price_cny DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格（人民币）',
    price_usd DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格（美元）',
    duration_days INT NOT NULL DEFAULT 30 COMMENT '有效天数（0=永久）',
    features JSON COMMENT '功能列表，如["导出报告","批量对比","无限对比"]',
    max_compare_robots INT NOT NULL DEFAULT 3 COMMENT '最大对比数量',
    max_exports_per_day INT NOT NULL DEFAULT 0 COMMENT '每日导出次数（0=不限）',
    api_access TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有API访问权限',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员套餐';

-- 用户会员表
CREATE TABLE user_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date DATETIME COMMENT '到期时间（NULL=永久）',
    status ENUM('active','expired','cancelled') NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES membership_plans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会员';

-- 支付订单表
CREATE TABLE payment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    amount_cny DECIMAL(10,2) NOT NULL,
    payment_method ENUM('wechat','alipay','stripe','manual') NOT NULL DEFAULT 'manual',
    status ENUM('pending','paid','failed','refunded','cancelled') NOT NULL DEFAULT 'pending',
    paid_at DATETIME,
    third_party_order_id VARCHAR(200) COMMENT '第三方支付订单号',
    remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES membership_plans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单';

-- 插入默认套餐
INSERT INTO membership_plans (name, name_en, description, price_cny, price_usd, duration_days, features, max_compare_robots, max_exports_per_day, api_access, sort_order) VALUES
('免费版', 'Free', '基础功能，永久免费', 0, 0, 0, '["基础搜索","最多对比3款","浏览机器人详情"]', 3, 0, 0, 0),
('专业版', 'Pro', '适合个人用户和研究人员', 99, 14, 30, '["高级搜索","最多对比10款","导出PDF报告","邮件提醒"]', 10, 5, 0, 1),
('企业版', 'Enterprise', '适合企业采购和集成商', 299, 42, 30, '["全部功能","无限对比","无限导出","API访问","专属客服"]', 999, 0, 1, 2);
