
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;
SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'd8ac5586-d662-11f0-af0b-45726ed0c980:1-2658';
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `ad_click_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ad_id` bigint NOT NULL COMMENT '广告ID，关联 ad_placements.id',
  `user_id` bigint DEFAULT NULL COMMENT '点击用户ID，NULL 表示未登录访客',
  `ip` varchar(50) DEFAULT NULL COMMENT '访客 IP 地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器 User-Agent',
  `clicked_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点击时间',
  PRIMARY KEY (`id`),
  KEY `idx_ad_id` (`ad_id`),
  KEY `idx_clicked_at` (`clicked_at`),
  CONSTRAINT `ad_click_logs_ibfk_1` FOREIGN KEY (`ad_id`) REFERENCES `ad_placements` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告点击行为日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `ad_placements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(300) NOT NULL COMMENT '广告标题',
  `description` varchar(500) DEFAULT NULL COMMENT '广告描述',
  `image_url` varchar(500) DEFAULT NULL COMMENT '广告图片 URL',
  `link_url` varchar(500) DEFAULT NULL COMMENT '点击跳转目标 URL',
  `position` enum('home_banner','list_promoted','detail_sidebar','search_top') NOT NULL COMMENT '广告投放位置',
  `ad_type` enum('banner','card','text') NOT NULL DEFAULT 'banner' COMMENT '广告形式：banner=横幅，card=卡片，text=文字链',
  `priority` int NOT NULL DEFAULT '0' COMMENT '展示优先级，值越大越靠前',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=暂停，1=投放中',
  `start_date` date DEFAULT NULL COMMENT '投放开始日期',
  `end_date` date DEFAULT NULL COMMENT '投放结束日期',
  `manufacturer_id` bigint DEFAULT NULL COMMENT '关联厂商ID（可选），关联 manufacturers.id',
  `impression_count` bigint NOT NULL DEFAULT '0' COMMENT '累计展示次数',
  `click_count` bigint NOT NULL DEFAULT '0' COMMENT '累计点击次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position_active` (`position`,`is_active`),
  KEY `idx_manufacturer_id` (`manufacturer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告投放配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `application_domains` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '应用领域中文名称，如：汽车制造',
  `name_en` varchar(100) DEFAULT NULL COMMENT '应用领域英文名称',
  `slug` varchar(100) NOT NULL COMMENT 'URL 友好标识，全小写连字符',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人应用领域标签表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `case_studies` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '案例作者用户ID，关联 users.id',
  `title` varchar(300) NOT NULL COMMENT '案例标题',
  `content` text NOT NULL COMMENT '案例详情，Markdown 格式',
  `robot_ids` json DEFAULT NULL COMMENT '案例中使用的机器人ID数组，如：[1,2]',
  `industry` varchar(100) DEFAULT NULL COMMENT '所属行业，如：汽车制造/物流仓储',
  `images` json DEFAULT NULL COMMENT '案例图片 URL 数组',
  `status` enum('draft','pending_review','published','rejected') NOT NULL DEFAULT 'draft' COMMENT '状态：draft=草稿，pending_review=待审，published=已发布，rejected=已拒绝',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_industry` (`industry`),
  CONSTRAINT `case_studies_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户生成的机器人应用案例（UGC）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '所属会话ID，关联 chat_sessions.id',
  `role` varchar(20) NOT NULL COMMENT '消息角色：user=用户，assistant=AI',
  `content` text NOT NULL COMMENT '消息内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 对话消息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `chat_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_key` varchar(100) NOT NULL COMMENT '会话唯一标识，前端生成 UUID',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID，NULL 表示匿名会话',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_key` (`session_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_key` (`session_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 对话会话表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `compare_sessions` (
  `id` varchar(64) NOT NULL COMMENT '对比会话唯一ID（UUID）',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，NULL 表示匿名用户',
  `robot_ids` json NOT NULL COMMENT '参与对比的机器人ID数组，如：[1,2,3]',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间，过期后自动清理',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人对比会话表，存储用户的对比选择';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `crawler_item_hashes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_id` bigint NOT NULL COMMENT '数据源ID，关联 crawler_sources.id',
  `item_url` varchar(1000) NOT NULL COMMENT '条目详情页 URL',
  `content_hash` varchar(64) NOT NULL COMMENT '页面内容 SHA256 哈希，用于增量检测',
  `robot_id` bigint DEFAULT NULL COMMENT '对应写入的机器人ID，关联 robots.id',
  `last_crawled_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近爬取时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_url` (`source_id`,`item_url`(500)),
  KEY `idx_source_id` (`source_id`),
  CONSTRAINT `crawler_item_hashes_ibfk_1` FOREIGN KEY (`source_id`) REFERENCES `crawler_sources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='爬虫增量内容哈希表，避免重复写入未变化数据';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `crawler_run_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_id` bigint NOT NULL COMMENT '数据源ID，关联 crawler_sources.id',
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '爬取开始时间',
  `finished_at` datetime DEFAULT NULL COMMENT '爬取结束时间',
  `status` varchar(20) NOT NULL DEFAULT 'running' COMMENT '执行状态：running=进行中，success=成功，failed=失败，cancelled=已取消',
  `pages_crawled` int NOT NULL DEFAULT '0' COMMENT '本次爬取页数',
  `items_crawled` int NOT NULL DEFAULT '0' COMMENT '本次采集条目数',
  `items_upserted` int NOT NULL DEFAULT '0' COMMENT '本次写入/更新条目数',
  `items_skipped` int NOT NULL DEFAULT '0' COMMENT '本次跳过条目数（内容未变化）',
  `error_msg` text COMMENT '错误信息，失败时记录',
  `triggered_by` varchar(50) DEFAULT 'scheduler' COMMENT '触发方式：scheduler=定时，manual=手动',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_started_at` (`started_at`),
  CONSTRAINT `crawler_run_logs_ibfk_1` FOREIGN KEY (`source_id`) REFERENCES `crawler_sources` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='爬虫执行日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `crawler_sources` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) NOT NULL COMMENT '数据源名称，如：Unitree 官网',
  `description` text COMMENT '数据源描述',
  `url_pattern` varchar(500) NOT NULL COMMENT '起始 URL，多个用逗号分隔',
  `config` json NOT NULL COMMENT '爬取规则配置，包含选择器、字段映射、厂商信息等',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=禁用，1=启用',
  `cron_expr` varchar(100) DEFAULT '0 0 3 * * ?' COMMENT 'Cron 表达式，默认每天凌晨 3 点执行',
  `max_pages` int NOT NULL DEFAULT '10' COMMENT '单次爬取最大页数',
  `delay_ms` int NOT NULL DEFAULT '1000' COMMENT '请求间隔（毫秒），避免被封',
  `last_crawl_at` datetime DEFAULT NULL COMMENT '上次爬取完成时间',
  `last_crawl_status` varchar(20) DEFAULT NULL COMMENT '上次爬取状态：success/failed/running',
  `total_crawled` bigint NOT NULL DEFAULT '0' COMMENT '累计写入记录数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='爬虫数据源配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `inquiries` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '发起询价的用户ID，关联 users.id',
  `robot_id` bigint NOT NULL COMMENT '询价机器人ID，关联 robots.id',
  `manufacturer_id` bigint NOT NULL COMMENT '目标厂商ID，关联 manufacturers.id',
  `message` text NOT NULL COMMENT '询价内容/留言',
  `contact_name` varchar(100) DEFAULT NULL COMMENT '联系人姓名',
  `contact_email` varchar(200) NOT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `contact_company` varchar(200) DEFAULT NULL COMMENT '所在公司',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending=待处理，replied=已回复，closed=已关闭',
  `replied_at` datetime DEFAULT NULL COMMENT '厂商回复时间',
  `reply_content` text COMMENT '厂商回复内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '询价提交时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_robot_id` (`robot_id`),
  KEY `idx_manufacturer_id` (`manufacturer_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_inquiry_manufacturer` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturers` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inquiry_robot` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inquiry_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户对机器人的询价记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `manufacturer_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '发起申请的用户ID，关联 users.id',
  `manufacturer_id` bigint DEFAULT NULL COMMENT '认领已有厂商的ID，NULL 表示新建厂商',
  `company_name` varchar(300) NOT NULL COMMENT '申请公司中文名称',
  `company_name_en` varchar(300) DEFAULT NULL COMMENT '申请公司英文名称',
  `country` varchar(100) NOT NULL COMMENT '公司所在国家',
  `website_url` varchar(500) DEFAULT NULL COMMENT '公司官网 URL',
  `contact_person` varchar(100) NOT NULL COMMENT '联系人姓名',
  `contact_email` varchar(200) NOT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `business_license` varchar(500) DEFAULT NULL COMMENT '营业执照图片 URL',
  `description` text COMMENT '申请说明',
  `status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending=待审，approved=通过，rejected=拒绝',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `approved_at` datetime DEFAULT NULL COMMENT '审核通过时间',
  `approved_by_user_id` bigint DEFAULT NULL COMMENT '审核管理员用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `manufacturer_applications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='厂商入驻认证申请记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `manufacturer_users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `manufacturer_id` bigint NOT NULL COMMENT '厂商ID，关联 manufacturers.id',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 users.id',
  `role` enum('owner','admin','member') NOT NULL DEFAULT 'member' COMMENT '角色：owner=所有者，admin=管理员，member=普通成员',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mfr_user` (`manufacturer_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `manufacturer_users_ibfk_1` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturers` (`id`),
  CONSTRAINT `manufacturer_users_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='厂商账号管理员关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `manufacturers` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) NOT NULL COMMENT '厂商中文名称',
  `name_en` varchar(200) DEFAULT NULL COMMENT '厂商英文名称',
  `country` varchar(100) NOT NULL COMMENT '所在国家（中文）',
  `country_code` char(2) DEFAULT NULL COMMENT '国家代码 ISO 3166-1 Alpha-2，如 CN/US/DE',
  `logo_url` varchar(500) DEFAULT NULL COMMENT '厂商 Logo 图片 URL',
  `website_url` varchar(500) DEFAULT NULL COMMENT '官网首页 URL',
  `founded_year` smallint DEFAULT NULL COMMENT '成立年份',
  `description` text COMMENT '厂商中文简介',
  `description_en` text COMMENT '厂商英文简介',
  `employee_count` int DEFAULT NULL COMMENT '员工人数',
  `headquarters` varchar(300) DEFAULT NULL COMMENT '总部地址',
  `stock_code` varchar(50) DEFAULT NULL COMMENT '股票代码（如已上市）',
  `contact_person` varchar(100) DEFAULT NULL COMMENT '联系人姓名',
  `contact_email` varchar(200) DEFAULT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `business_license` varchar(500) DEFAULT NULL COMMENT '营业执照图片 URL',
  `company_size` varchar(50) DEFAULT NULL COMMENT '公司规模，如：1-50/51-200/201-500/500+',
  `verified_at` datetime DEFAULT NULL COMMENT '厂商认证通过时间',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT '是否已认证：0=未认证，1=已认证',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，值越大越靠前',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_country` (`country_code`),
  KEY `idx_status` (`status`),
  FULLTEXT KEY `ft_name` (`name`,`name_en`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人厂商信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `membership_plans` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '套餐名称，如：免费版/专业版/企业版',
  `name_en` varchar(100) DEFAULT NULL COMMENT '套餐英文名称',
  `description` text COMMENT '套餐描述',
  `price_cny` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格（人民币）',
  `price_usd` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格（美元）',
  `duration_days` int NOT NULL DEFAULT '30' COMMENT '有效天数，0=永久有效',
  `features` json DEFAULT NULL COMMENT '功能权益列表，JSON 数组，如：["导出报告","批量对比"]',
  `max_compare_robots` int NOT NULL DEFAULT '3' COMMENT '最大同时对比机器人数量',
  `max_exports_per_day` int NOT NULL DEFAULT '0' COMMENT '每日最大导出次数，0=不限',
  `api_access` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否开放 API 访问权限：0=否，1=是',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否上架：0=下架，1=上架',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员套餐配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID，关联 users.id',
  `type` varchar(50) NOT NULL COMMENT '通知类型，如：inquiry_reply/review_approved/price_alert/system',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知详细内容',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0=未读，1=已读',
  `related_id` bigint DEFAULT NULL COMMENT '关联资源ID，配合 related_type 使用',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联资源类型，如：robot/inquiry/review/order',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id_read` (`user_id`,`is_read`),
  KEY `idx_user_id_created` (`user_id`,`created_at`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户站内消息通知表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `payment_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号，系统生成唯一编号',
  `user_id` bigint NOT NULL COMMENT '下单用户ID，关联 users.id',
  `plan_id` bigint NOT NULL COMMENT '购买的套餐ID，关联 membership_plans.id',
  `amount_cny` decimal(10,2) NOT NULL COMMENT '实付金额（人民币）',
  `payment_method` enum('wechat','alipay','stripe','manual') NOT NULL DEFAULT 'manual' COMMENT '支付方式：wechat=微信支付，alipay=支付宝，stripe=Stripe，manual=手动开通',
  `status` enum('pending','paid','failed','refunded','cancelled') NOT NULL DEFAULT 'pending' COMMENT '订单状态：pending=待支付，paid=已支付，failed=支付失败，refunded=已退款，cancelled=已取消',
  `paid_at` datetime DEFAULT NULL COMMENT '支付成功时间',
  `third_party_order_id` varchar(200) DEFAULT NULL COMMENT '第三方支付平台订单号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `plan_id` (`plan_id`),
  CONSTRAINT `payment_orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `payment_orders_ibfk_2` FOREIGN KEY (`plan_id`) REFERENCES `membership_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员支付订单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `price_alerts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '设置提醒的用户ID，关联 users.id',
  `robot_id` bigint NOT NULL COMMENT '监控的机器人ID，关联 robots.id',
  `target_price` decimal(12,2) NOT NULL COMMENT '目标价格，低于此价格时触发提醒',
  `is_triggered` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已触发：0=未触发，1=已触发',
  `triggered_at` datetime DEFAULT NULL COMMENT '触发时间',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效：0=已关闭，1=监控中',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_robot` (`user_id`,`robot_id`),
  KEY `idx_robot_id` (`robot_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人降价提醒设置表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_3d_models` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '所属机器人ID，关联 robots.id，一对一',
  `model_url` varchar(500) DEFAULT NULL COMMENT '3D 模型文件 URL（GLB/GLTF 格式）',
  `poster_url` varchar(500) DEFAULT NULL COMMENT '模型预览封面图 URL',
  `ar_url` varchar(500) DEFAULT NULL COMMENT 'AR 模型 URL（USDZ 格式，用于 iOS AR Quick Look）',
  `title` varchar(200) DEFAULT NULL COMMENT '模型标题',
  `annotations` json DEFAULT NULL COMMENT '模型标注点数组，JSON 格式，如：[{position,label,description}]',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `robot_id` (`robot_id`),
  KEY `idx_robot_id` (`robot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人 3D 模型资源表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_application_domains` (
  `robot_id` bigint NOT NULL COMMENT '机器人ID，关联 robots.id',
  `domain_id` bigint NOT NULL COMMENT '应用领域ID，关联 application_domains.id',
  PRIMARY KEY (`robot_id`,`domain_id`),
  KEY `domain_id` (`domain_id`),
  CONSTRAINT `robot_application_domains_ibfk_1` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `robot_application_domains_ibfk_2` FOREIGN KEY (`domain_id`) REFERENCES `application_domains` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人与应用领域多对多关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父分类ID，NULL 表示顶级分类',
  `name` varchar(100) NOT NULL COMMENT '分类中文名称',
  `name_en` varchar(100) DEFAULT NULL COMMENT '分类英文名称',
  `slug` varchar(100) NOT NULL COMMENT 'URL 友好标识，全小写连字符，唯一',
  `icon` varchar(200) DEFAULT NULL COMMENT '分类图标 URL 或 icon class',
  `description` text COMMENT '分类描述',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，值越大越靠前',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人分类表，支持多级分类';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '评论所属机器人ID，关联 robots.id',
  `user_id` bigint NOT NULL COMMENT '评论用户ID，关联 users.id',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL=顶级评论，非NULL=回复',
  `content` text NOT NULL COMMENT '评论内容',
  `rating` tinyint DEFAULT NULL COMMENT '评分 1-5，顶级评论才有',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `status` enum('pending','approved','rejected') DEFAULT 'approved' COMMENT '审核状态：pending=待审，approved=通过，rejected=拒绝',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot` (`robot_id`,`status`),
  KEY `idx_user` (`user_id`),
  KEY `idx_robot_time` (`robot_id`,`created_at`),
  CONSTRAINT `robot_comments_ibfk_1` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `robot_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人用户评论表，支持嵌套回复';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_documents` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '所属机器人ID，关联 robots.id',
  `title` varchar(300) NOT NULL COMMENT '文档标题',
  `url` varchar(500) NOT NULL COMMENT '文档下载 URL',
  `doc_type` enum('datasheet','manual','brochure','certificate','other') DEFAULT 'datasheet' COMMENT '文档类型：datasheet=规格表，manual=用户手册，brochure=宣传册，certificate=认证文件',
  `file_size` int DEFAULT NULL COMMENT '文件大小（字节）',
  `language` varchar(20) DEFAULT 'zh' COMMENT '文档语言代码，如：zh/en/de',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot` (`robot_id`),
  CONSTRAINT `robot_documents_ibfk_1` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人技术文档与资料表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '所属机器人ID，关联 robots.id',
  `url` varchar(500) NOT NULL COMMENT '图片完整 URL',
  `thumbnail` varchar(500) DEFAULT NULL COMMENT '缩略图 URL',
  `alt_text` varchar(300) DEFAULT NULL COMMENT '图片 alt 描述文本，用于 SEO 和无障碍',
  `image_type` enum('product','detail','scene','certificate') DEFAULT 'product' COMMENT '图片类型：product=产品图，detail=细节图，scene=应用场景图，certificate=认证证书',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，值越小越靠前',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot` (`robot_id`),
  CONSTRAINT `robot_images_ibfk_1` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人图片资源表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_price_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '机器人ID，关联 robots.id',
  `price` decimal(12,2) NOT NULL COMMENT '价格',
  `currency` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '货币单位，如：CNY/USD/EUR',
  `source` varchar(100) DEFAULT NULL COMMENT '数据来源，如：官网/经销商/爬虫',
  `recorded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '价格记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot_id_recorded` (`robot_id`,`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人价格历史记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '评测作者用户ID，关联 users.id',
  `robot_id` bigint NOT NULL COMMENT '评测的机器人ID，关联 robots.id',
  `title` varchar(300) NOT NULL COMMENT '评测标题',
  `content` text NOT NULL COMMENT '评测正文，Markdown 格式',
  `pros` text COMMENT '优点描述',
  `cons` text COMMENT '缺点描述',
  `rating` tinyint NOT NULL DEFAULT '5' COMMENT '综合评分 1-5 分',
  `images` json DEFAULT NULL COMMENT '评测图片 URL 数组',
  `status` enum('draft','pending_review','published','rejected') NOT NULL DEFAULT 'draft' COMMENT '状态：draft=草稿，pending_review=待审，published=已发布，rejected=已拒绝',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot_id` (`robot_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `robot_reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `robot_reviews_ibfk_2` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户生成的机器人评测内容（UGC）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_tag_mappings` (
  `robot_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`robot_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_rtm_robot` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rtm_tag` FOREIGN KEY (`tag_id`) REFERENCES `robot_tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `slug` varchar(100) NOT NULL,
  `usage_count` int DEFAULT '0' COMMENT '引用计数，方便排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_videos` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '所属机器人ID，关联 robots.id',
  `title` varchar(300) DEFAULT NULL COMMENT '视频标题',
  `url` varchar(500) NOT NULL COMMENT '视频 URL（YouTube/Bilibili 嵌入链接或直链）',
  `thumbnail` varchar(500) DEFAULT NULL COMMENT '视频封面图 URL',
  `duration_s` int DEFAULT NULL COMMENT '视频时长（秒）',
  `video_type` enum('product','demo','installation','official') DEFAULT 'product' COMMENT '视频类型：product=产品介绍，demo=演示，installation=安装教程，official=官方宣传',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，值越小越靠前',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot` (`robot_id`),
  CONSTRAINT `robot_videos_ibfk_1` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人视频资源表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robot_view_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `robot_id` bigint NOT NULL COMMENT '被浏览的机器人ID，关联 robots.id',
  `user_id` bigint DEFAULT NULL COMMENT '登录用户ID，NULL 表示未登录',
  `session_id` varchar(64) DEFAULT NULL COMMENT '匿名会话ID，用于未登录用户追踪',
  `ip_address` varchar(45) DEFAULT NULL COMMENT '访客 IP 地址（兼容 IPv6）',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器 User-Agent 字符串',
  `viewed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_robot_date` (`robot_id`,`viewed_at`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人页面浏览日志，用于热度统计和推荐算法';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `robots` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `manufacturer_id` bigint NOT NULL COMMENT '所属厂商ID，关联 manufacturers.id',
  `category_id` bigint NOT NULL COMMENT '所属分类ID，关联 robot_categories.id',
  `name` varchar(300) NOT NULL COMMENT '机器人中文名称',
  `name_en` varchar(300) DEFAULT NULL COMMENT '机器人英文名称',
  `subtitle` varchar(500) DEFAULT NULL,
  `model_number` varchar(200) DEFAULT NULL COMMENT '型号编号，如：UR5e、GP4',
  `slug` varchar(300) NOT NULL COMMENT 'URL 友好唯一标识，用于 SEO 路由',
  `description` text COMMENT '中文详细描述',
  `description_en` text COMMENT '英文详细描述',
  `introduction` text,
  `application_scenarios` text,
  `advantages` text,
  `release_year` smallint DEFAULT NULL COMMENT '发布年份',
  `status` enum('active','discontinued','upcoming') DEFAULT 'active' COMMENT '状态：active=在售，discontinued=停产，upcoming=即将发布',
  `dof` tinyint DEFAULT NULL COMMENT '自由度（Degrees of Freedom）',
  `payload_kg` decimal(10,3) DEFAULT NULL COMMENT '最大负载（kg）',
  `reach_mm` int DEFAULT NULL COMMENT '最大臂展/作业半径（mm）',
  `repeatability_mm` decimal(8,4) DEFAULT NULL COMMENT '重复定位精度（mm），值越小越精准',
  `max_speed_deg_s` decimal(10,2) DEFAULT NULL COMMENT '最大关节速度（°/s）',
  `weight_kg` decimal(10,2) DEFAULT NULL COMMENT '机器人本体重量（kg）',
  `ip_rating` varchar(20) DEFAULT NULL COMMENT '防护等级，如：IP54、IP67',
  `mounting` varchar(100) DEFAULT NULL COMMENT '安装方式，如：地面/倒装/壁挂',
  `max_speed_m_s` decimal(6,3) DEFAULT NULL COMMENT '最大移动速度（m/s）',
  `max_load_kg` decimal(10,2) DEFAULT NULL COMMENT '最大载重（kg），适用于 AGV/AMR',
  `battery_life_h` decimal(5,1) DEFAULT NULL COMMENT '续航时间（小时）',
  `navigation_type` varchar(200) DEFAULT NULL COMMENT '导航方式，如：SLAM/二维码/磁条',
  `height_mm` int DEFAULT NULL COMMENT '机器人整机高度（mm）',
  `walking_speed_m_s` decimal(5,3) DEFAULT NULL COMMENT '行走速度（m/s）',
  `extra_specs` json DEFAULT NULL COMMENT '扩展规格参数，键值对 JSON，存放非标准参数',
  `price_range` varchar(100) DEFAULT 'inquiry',
  `price_usd_from` decimal(12,2) DEFAULT NULL COMMENT '参考起售价（美元），0 表示面议',
  `cover_image_url` varchar(500) DEFAULT NULL COMMENT '封面主图 URL',
  `content_images` json DEFAULT NULL,
  `video_urls` json DEFAULT NULL,
  `is_open_source` varchar(20) DEFAULT NULL,
  `listed_date` date DEFAULT NULL,
  `source_url` varchar(500) DEFAULT NULL COMMENT '官网产品详情页 URL，爬虫采集来源地址',
  `model_3d_url` varchar(500) DEFAULT NULL COMMENT '3D 模型文件 URL（GLB/GLTF 格式）',
  `model_3d_preview` varchar(500) DEFAULT NULL COMMENT '3D 模型预览图 URL',
  `has_3d_model` tinyint(1) DEFAULT '0' COMMENT '是否有 3D 模型：0=无，1=有',
  `has_video` tinyint(1) DEFAULT '0' COMMENT '是否有视频：0=无，1=有',
  `view_count` bigint DEFAULT '0' COMMENT '浏览次数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏次数',
  `compare_count` int DEFAULT '0' COMMENT '被加入对比次数',
  `is_featured` tinyint(1) DEFAULT '0' COMMENT '是否精选推荐：0=否，1=是',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT '是否已由厂商认证：0=否，1=是',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，值越大越靠前',
  `created_by` bigint DEFAULT NULL COMMENT '创建者用户ID，NULL 表示系统导入',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `idx_manufacturer` (`manufacturer_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payload` (`payload_kg`),
  KEY `idx_reach` (`reach_mm`),
  KEY `idx_featured` (`is_featured`,`sort_order`),
  KEY `idx_release_year` (`release_year`,`status`),
  KEY `idx_view_count` (`view_count`,`status`),
  KEY `idx_category_status_payload` (`category_id`,`status`,`payload_kg`),
  KEY `idx_manufacturer_status` (`manufacturer_id`,`status`),
  KEY `idx_category_status_featured` (`category_id`,`status`,`is_featured`,`sort_order`,`view_count`),
  FULLTEXT KEY `ft_search` (`name`,`name_en`,`model_number`,`description`),
  CONSTRAINT `robots_ibfk_1` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturers` (`id`),
  CONSTRAINT `robots_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `robot_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机器人产品核心信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `search_hot_keywords` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(200) NOT NULL COMMENT '搜索关键词',
  `search_count` bigint NOT NULL DEFAULT '1' COMMENT '累计搜索次数',
  `last_searched_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近搜索时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次搜索时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_search_count` (`search_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='搜索热词统计表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `ugc_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID，关联 users.id',
  `target_type` enum('review','case_study') NOT NULL COMMENT '点赞目标类型：review=评测，case_study=案例',
  `target_id` bigint NOT NULL COMMENT '点赞目标ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`),
  CONSTRAINT `ugc_likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='UGC 内容点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `ugc_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '举报用户ID，关联 users.id',
  `target_type` enum('review','case_study') NOT NULL COMMENT '举报目标类型',
  `target_id` bigint NOT NULL COMMENT '被举报内容ID',
  `reason` varchar(500) NOT NULL COMMENT '举报原因描述',
  `status` enum('pending','handled') NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending=待处理，handled=已处理',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `ugc_reports_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='UGC 内容违规举报记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_action_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，未登录为 NULL',
  `session_id` varchar(100) NOT NULL COMMENT '前端会话ID',
  `action_type` enum('view','favorite','compare','search','click','share') NOT NULL COMMENT '行为类型',
  `target_type` enum('robot','category','manufacturer','search') NOT NULL COMMENT '目标资源类型',
  `target_id` bigint DEFAULT NULL COMMENT '目标资源ID',
  `target_slug` varchar(300) DEFAULT NULL COMMENT '目标资源 slug',
  `extra_data` json DEFAULT NULL COMMENT '扩展数据，如搜索词、来源页等',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '访客 IP 地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器 User-Agent',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_session` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_time` (`user_id`,`created_at`),
  KEY `idx_target_action_time` (`target_id`,`action_type`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为日志表，用于统计与推荐算法';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 users.id',
  `robot_id` bigint NOT NULL COMMENT '收藏的机器人ID，关联 robots.id',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_robot` (`user_id`,`robot_id`),
  KEY `robot_id` (`robot_id`),
  KEY `idx_user_time` (`user_id`,`created_at`),
  CONSTRAINT `user_favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_favorites_ibfk_2` FOREIGN KEY (`robot_id`) REFERENCES `robots` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏机器人记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_memberships` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 users.id',
  `plan_id` bigint NOT NULL COMMENT '套餐ID，关联 membership_plans.id',
  `start_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会员开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '会员到期时间，NULL=永久有效',
  `status` enum('active','expired','cancelled') NOT NULL DEFAULT 'active' COMMENT '状态：active=有效，expired=已过期，cancelled=已取消',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `plan_id` (`plan_id`),
  CONSTRAINT `user_memberships_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_memberships_ibfk_2` FOREIGN KEY (`plan_id`) REFERENCES `membership_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会员订阅记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_oauth_bindings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID，关联 users.id',
  `provider` varchar(30) NOT NULL COMMENT '第三方登录平台：google / wechat_web / wechat_miniapp',
  `provider_user_id` varchar(255) NOT NULL COMMENT '第三方平台的用户唯一标识（OpenID/Sub）',
  `access_token` varchar(1000) DEFAULT NULL COMMENT '第三方平台 Access Token，定期刷新',
  `refresh_token` varchar(1000) DEFAULT NULL COMMENT '第三方平台 Refresh Token',
  `extra_data` text COMMENT '额外信息 JSON，存储头像/昵称等第三方返回数据',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_user` (`provider`,`provider_user_id`),
  KEY `fk_oauth_user` (`user_id`),
  CONSTRAINT `fk_oauth_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户第三方社交账号绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(200) NOT NULL COMMENT '邮箱地址，唯一，用于登录',
  `username` varchar(100) DEFAULT NULL COMMENT '用户名，唯一，可选',
  `password_hash` varchar(255) DEFAULT NULL COMMENT '密码哈希值，OAuth 用户可为 NULL',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像图片 URL',
  `display_name` varchar(100) DEFAULT NULL COMMENT '显示昵称',
  `role` enum('user','admin','superadmin') DEFAULT 'user' COMMENT '用户角色：user=普通用户，admin=管理员，superadmin=超级管理员',
  `company` varchar(200) DEFAULT NULL COMMENT '所在公司名称',
  `job_title` varchar(200) DEFAULT NULL COMMENT '职位/职称',
  `country` varchar(100) DEFAULT NULL COMMENT '所在国家',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '账号状态：0=禁用，1=正常',
  `email_verified` tinyint(1) DEFAULT '0' COMMENT '邮箱是否已验证：0=未验证，1=已验证',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户账号表';
/*!40101 SET character_set_client = @saved_cs_client */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

