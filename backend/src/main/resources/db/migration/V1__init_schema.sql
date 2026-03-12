-- LookForBest 初始化数据库结构
-- Flyway V1 | 2026-03-09

-- 厂商表
CREATE TABLE IF NOT EXISTS manufacturers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '厂商名称',
    name_en         VARCHAR(200) COMMENT '英文名称',
    country         VARCHAR(100) NOT NULL COMMENT '国家',
    country_code    CHAR(2) COMMENT '国家代码',
    logo_url        VARCHAR(500) COMMENT 'Logo图片URL',
    website_url     VARCHAR(500) COMMENT '官网URL',
    founded_year    SMALLINT COMMENT '成立年份',
    description     TEXT COMMENT '厂商简介',
    description_en  TEXT COMMENT '英文简介',
    employee_count  INT COMMENT '员工人数',
    headquarters    VARCHAR(300) COMMENT '总部地址',
    stock_code      VARCHAR(50) COMMENT '股票代码',
    is_verified     TINYINT(1) DEFAULT 0,
    sort_order      INT DEFAULT 0,
    status          TINYINT(1) DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_country (country_code),
    INDEX idx_status (status),
    FULLTEXT ft_name (name, name_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机器人分类表
CREATE TABLE IF NOT EXISTS robot_categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT DEFAULT NULL,
    name        VARCHAR(100) NOT NULL,
    name_en     VARCHAR(100),
    slug        VARCHAR(100) NOT NULL UNIQUE,
    icon        VARCHAR(200),
    description TEXT,
    sort_order  INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 应用领域表
CREATE TABLE IF NOT EXISTS application_domains (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    name_en     VARCHAR(100),
    slug        VARCHAR(100) NOT NULL UNIQUE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机器人核心表
CREATE TABLE IF NOT EXISTS robots (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_id     BIGINT NOT NULL,
    category_id         BIGINT NOT NULL,
    name                VARCHAR(300) NOT NULL,
    name_en             VARCHAR(300),
    model_number        VARCHAR(200),
    slug                VARCHAR(300) NOT NULL UNIQUE,
    description         TEXT,
    description_en      TEXT,
    release_year        SMALLINT,
    status              ENUM('active','discontinued','upcoming') DEFAULT 'active',
    dof                 TINYINT,
    payload_kg          DECIMAL(10,3),
    reach_mm            INT,
    repeatability_mm    DECIMAL(8,4),
    max_speed_deg_s     DECIMAL(10,2),
    weight_kg           DECIMAL(10,2),
    ip_rating           VARCHAR(20),
    mounting            VARCHAR(100),
    max_speed_m_s       DECIMAL(6,3),
    max_load_kg         DECIMAL(10,2),
    battery_life_h      DECIMAL(5,1),
    navigation_type     VARCHAR(200),
    height_mm           INT,
    walking_speed_m_s   DECIMAL(5,3),
    extra_specs         JSON,
    price_range         ENUM('inquiry','low','medium','high','premium') DEFAULT 'inquiry',
    price_usd_from      DECIMAL(12,2),
    cover_image_url     VARCHAR(500),
    model_3d_url        VARCHAR(500),
    model_3d_preview    VARCHAR(500),
    has_3d_model        TINYINT(1) DEFAULT 0,
    has_video           TINYINT(1) DEFAULT 0,
    view_count          BIGINT DEFAULT 0,
    favorite_count      INT DEFAULT 0,
    compare_count       INT DEFAULT 0,
    is_featured         TINYINT(1) DEFAULT 0,
    is_verified         TINYINT(1) DEFAULT 0,
    sort_order          INT DEFAULT 0,
    created_by          BIGINT,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(id),
    FOREIGN KEY (category_id) REFERENCES robot_categories(id),
    INDEX idx_manufacturer (manufacturer_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_payload (payload_kg),
    INDEX idx_reach (reach_mm),
    INDEX idx_featured (is_featured, sort_order),
    INDEX idx_category_status_payload (category_id, status, payload_kg),
    INDEX idx_manufacturer_status (manufacturer_id, status),
    FULLTEXT ft_search (name, name_en, model_number, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机器人应用领域关联表
CREATE TABLE IF NOT EXISTS robot_application_domains (
    robot_id    BIGINT NOT NULL,
    domain_id   BIGINT NOT NULL,
    PRIMARY KEY (robot_id, domain_id),
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
    FOREIGN KEY (domain_id) REFERENCES application_domains(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机器人图片表
CREATE TABLE IF NOT EXISTS robot_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT NOT NULL,
    url         VARCHAR(500) NOT NULL,
    thumbnail   VARCHAR(500),
    alt_text    VARCHAR(300),
    image_type  ENUM('product','detail','scene','certificate') DEFAULT 'product',
    sort_order  INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
    INDEX idx_robot (robot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 机器人视频表
CREATE TABLE IF NOT EXISTS robot_videos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT NOT NULL,
    title       VARCHAR(300),
    url         VARCHAR(500) NOT NULL,
    thumbnail   VARCHAR(500),
    duration_s  INT,
    video_type  ENUM('product','demo','installation','official') DEFAULT 'product',
    sort_order  INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
    INDEX idx_robot (robot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 技术文档表
CREATE TABLE IF NOT EXISTS robot_documents (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT NOT NULL,
    title       VARCHAR(300) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    doc_type    ENUM('datasheet','manual','brochure','certificate','other') DEFAULT 'datasheet',
    file_size   INT,
    language    VARCHAR(20) DEFAULT 'zh',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
    INDEX idx_robot (robot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(200) NOT NULL UNIQUE,
    username        VARCHAR(100) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    avatar_url      VARCHAR(500),
    display_name    VARCHAR(100),
    role            ENUM('user','admin','superadmin') DEFAULT 'user',
    company         VARCHAR(200),
    job_title       VARCHAR(200),
    country         VARCHAR(100),
    is_active       TINYINT(1) DEFAULT 1,
    email_verified  TINYINT(1) DEFAULT 0,
    last_login_at   DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorites (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    robot_id    BIGINT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_robot (user_id, robot_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 对比会话表
CREATE TABLE IF NOT EXISTS compare_sessions (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     BIGINT,
    robot_ids   JSON NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS robot_comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    parent_id   BIGINT DEFAULT NULL,
    content     TEXT NOT NULL,
    rating      TINYINT,
    like_count  INT DEFAULT 0,
    status      ENUM('pending','approved','rejected') DEFAULT 'approved',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_robot (robot_id, status),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 浏览日志表（用于统计与推荐）
CREATE TABLE IF NOT EXISTS robot_view_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT NOT NULL,
    user_id     BIGINT COMMENT '登录用户ID',
    session_id  VARCHAR(64) COMMENT '匿名Session',
    ip_address  VARCHAR(45) COMMENT 'IP地址',
    user_agent  VARCHAR(500) COMMENT 'UA',
    viewed_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_robot_date (robot_id, viewed_at),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
