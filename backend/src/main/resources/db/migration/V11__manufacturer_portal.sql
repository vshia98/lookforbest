-- 扩展 manufacturers 表
ALTER TABLE manufacturers
    ADD COLUMN contact_person VARCHAR(100) COMMENT '联系人' AFTER is_verified,
    ADD COLUMN contact_email VARCHAR(200) COMMENT '联系邮箱' AFTER contact_person,
    ADD COLUMN contact_phone VARCHAR(50) COMMENT '联系电话' AFTER contact_email,
    ADD COLUMN business_license VARCHAR(500) COMMENT '营业执照URL' AFTER contact_phone,
    ADD COLUMN company_size VARCHAR(50) COMMENT '公司规模' AFTER business_license,
    ADD COLUMN verified_at DATETIME COMMENT '认证时间' AFTER founded_year;

-- 厂商入驻申请表
CREATE TABLE manufacturer_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请用户',
    manufacturer_id BIGINT COMMENT '已存在厂商ID（认领）',
    company_name VARCHAR(300) NOT NULL COMMENT '公司名称',
    company_name_en VARCHAR(300),
    country VARCHAR(100) NOT NULL,
    website_url VARCHAR(500),
    contact_person VARCHAR(100) NOT NULL,
    contact_email VARCHAR(200) NOT NULL,
    contact_phone VARCHAR(50),
    business_license VARCHAR(500) COMMENT '营业执照图片URL',
    description TEXT COMMENT '申请说明',
    status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
    reject_reason VARCHAR(500),
    approved_at DATETIME,
    approved_by_user_id BIGINT COMMENT '审批管理员ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 厂商与用户关联表（一个厂商可有多个管理账号）
CREATE TABLE manufacturer_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('owner','admin','member') NOT NULL DEFAULT 'member',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mfr_user (manufacturer_id, user_id),
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
