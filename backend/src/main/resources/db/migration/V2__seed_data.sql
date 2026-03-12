-- 初始化基础数据
-- Flyway V2 | 2026-03-09

-- 机器人分类
INSERT IGNORE INTO robot_categories (name, name_en, slug, sort_order) VALUES
('工业机械臂', 'Industrial Robot Arm', 'industrial-arm', 1),
('协作机器人', 'Collaborative Robot', 'cobot', 2),
('移动机器人/AGV', 'Mobile Robot/AGV', 'mobile-agv', 3),
('人形机器人', 'Humanoid Robot', 'humanoid', 4),
('服务机器人', 'Service Robot', 'service', 5),
('特种机器人', 'Specialized Robot', 'specialized', 6),
('医疗机器人', 'Medical Robot', 'medical', 7),
('教育机器人', 'Educational Robot', 'educational', 8);

-- 应用领域
INSERT IGNORE INTO application_domains (name, name_en, slug) VALUES
('汽车制造', 'Automotive', 'automotive'),
('电子/半导体', 'Electronics', 'electronics'),
('物流仓储', 'Logistics', 'logistics'),
('食品饮料', 'Food & Beverage', 'food-beverage'),
('医疗健康', 'Healthcare', 'healthcare'),
('航空航天', 'Aerospace', 'aerospace'),
('金属加工', 'Metal Fabrication', 'metal'),
('3C消费电子', '3C Consumer Electronics', '3c-electronics'),
('餐饮服务', 'Catering', 'catering'),
('农业', 'Agriculture', 'agriculture');
