-- LookForBest 种子数据脚本
-- 版本: v1.0 | 日期: 2026-03-09
-- 密码说明: admin@lookforbest.com 密码为 Admin@123456 (BCrypt加密)

USE lookforbest;

-- =============================================
-- 厂商数据 (5个知名厂商)
-- =============================================
INSERT IGNORE INTO manufacturers (id, name, name_en, country, country_code, website_url, founded_year, description, headquarters, is_verified, sort_order, status) VALUES
(1, '波士顿动力', 'Boston Dynamics', '美国', 'US', 'https://www.bostondynamics.com', 1992,
 '波士顿动力是全球领先的移动机器人研发公司，以Spot机器狗和Atlas人形机器人闻名全球。',
 '美国马萨诸塞州沃尔瑟姆', 1, 10, 1),

(2, 'ABB', 'ABB Robotics', '瑞士', 'CH', 'https://new.abb.com/products/robotics', 1988,
 'ABB机器人业务部是全球最大的工业机器人供应商之一，提供工业机器人、协作机器人及相关服务。',
 '瑞士苏黎世', 1, 9, 1),

(3, '发那科', 'FANUC Corporation', '日本', 'JP', 'https://www.fanuc.co.jp', 1972,
 '发那科是全球最大的工业机器人制造商之一，专注于数控系统、工业机器人和工厂自动化。',
 '日本山梨县忍野村', 1, 9, 1),

(4, '优必选科技', 'UBTECH Robotics', '中国', 'CN', 'https://www.ubtrobot.com', 2012,
 '优必选科技是全球领先的人工智能与人形机器人研发企业，主打教育机器人和人形机器人。',
 '中国广东省深圳市', 1, 8, 1),

(5, '宇树科技', 'Unitree Robotics', '中国', 'CN', 'https://www.unitree.com', 2016,
 '宇树科技专注于高性能四足机器人和人形机器人的研发与制造，以高性价比著称。',
 '中国浙江省杭州市', 1, 8, 1);

-- =============================================
-- 机器人种子数据 (10个示例机器人)
-- =============================================

-- 1. ABB IRB 6700 工业机械臂
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    dof, payload_kg, reach_mm, repeatability_mm, max_speed_deg_s, weight_kg,
    ip_rating, mounting,
    price_range, cover_image_url, is_featured, is_verified, sort_order) VALUES
(1, 2, 1, 'IRB 6700', 'ABB IRB 6700', 'IRB 6700-235/2.65', 'abb-irb-6700',
 'ABB IRB 6700是专为繁重工业任务设计的大型工业机器人，广泛应用于汽车制造、金属加工等领域。载荷范围150-300kg，工作半径2.6-3.2m，是重载工业自动化的理想选择。',
 2016, 'active',
 6, 235.000, 2650, 0.0500, 120.00, 1340.00,
 'IP67', '地面/壁装/倒装',
 'high', 'https://via.placeholder.com/800x600?text=ABB+IRB+6700', 1, 1, 100);

-- 2. FANUC R-2000iC 工业机械臂
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    dof, payload_kg, reach_mm, repeatability_mm, max_speed_deg_s, weight_kg,
    ip_rating, mounting,
    price_range, cover_image_url, is_featured, is_verified, sort_order) VALUES
(2, 3, 1, 'R-2000iC/210F', 'FANUC R-2000iC', 'R-2000iC/210F', 'fanuc-r-2000ic-210f',
 'FANUC R-2000iC是发那科旗舰级大型工业机器人，以其超高可靠性和精度著称。适用于汽车焊接、搬运、冲压等重型工业场景。',
 2018, 'active',
 6, 210.000, 2655, 0.0500, 130.00, 1290.00,
 'IP67', '地面/壁装/倒装',
 'high', 'https://via.placeholder.com/800x600?text=FANUC+R-2000iC', 1, 1, 90);

-- 3. ABB YuMi 协作机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    dof, payload_kg, reach_mm, repeatability_mm, weight_kg,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(3, 2, 2, 'YuMi IRB 14000', 'ABB YuMi', 'IRB 14000', 'abb-yumi-irb-14000',
 'ABB YuMi是全球首款真正面向人机协作的双臂工业机器人，内置碰撞检测，无需安全围栏，适合电子、精密装配等场景。',
 2015, 'active',
 14, 0.500, 559, 0.0200, 38.00,
 'medium', 'https://via.placeholder.com/800x600?text=ABB+YuMi', 1, 1, 85,
 '{"arms": 2, "force_sensing": true, "safety_category": "PLd", "programming": "Lead-through"}');

-- 4. 波士顿动力 Spot 四足机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, max_speed_m_s, max_load_kg, battery_life_h,
    navigation_type, height_mm,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(4, 1, 3, 'Spot', 'Boston Dynamics Spot', 'Spot Enterprise', 'boston-dynamics-spot',
 'Spot是波士顿动力的四足机器人，能在复杂地形中自主导航，适用于工业巡检、安防监控、数据采集等场景。配备丰富的传感器接口，支持二次开发。',
 2020, 'active',
 32.00, 1.600, 14.00, 1.5,
 'SLAM激光雷达+视觉融合', 840,
 'premium', 'https://via.placeholder.com/800x600?text=Boston+Dynamics+Spot', 1, 1, 95,
 '{"degrees_of_freedom": 12, "payload_options": ["Spot Arm", "Spot CAM", "Spot CORE"], "ip_rating": "IP54", "operating_temp": "-20~45°C"}');

-- 5. 宇树 Go2 四足机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, max_speed_m_s, max_load_kg, battery_life_h,
    navigation_type, height_mm,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(5, 5, 3, 'Go2', 'Unitree Go2', 'Go2 Pro', 'unitree-go2',
 '宇树Go2是高性价比四足机器人，搭载英伟达Orin处理器，支持ROS2开发，适合科研、教育和工业检测场景。',
 2023, 'active',
 15.00, 3.500, 8.00, 2.0,
 '激光雷达SLAM+深度视觉', 710,
 'low', 'https://via.placeholder.com/800x600?text=Unitree+Go2', 1, 1, 80,
 '{"compute": "NVIDIA Jetson Orin", "ros_support": true, "obstacle_avoidance": true, "stair_climbing": true}');

-- 6. 优必选 Walker X 人形机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, height_mm, walking_speed_m_s, battery_life_h,
    dof,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(6, 4, 4, 'Walker X', 'UBTECH Walker X', 'Walker X', 'ubtech-walker-x',
 '优必选Walker X是先进的双足人形机器人，具备自然行走、手臂操控和AI交互能力，适用于商业服务、家庭陪伴和工业辅助等场景。',
 2021, 'active',
 76.00, 1700, 0.600, 3.0,
 41,
 'premium', 'https://via.placeholder.com/800x600?text=UBTECH+Walker+X', 1, 1, 88,
 '{"hands_dof": 12, "face_recognition": true, "voice_interaction": true, "object_grasping": true}');

-- 7. 宇树 H1 人形机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, height_mm, walking_speed_m_s, battery_life_h,
    dof,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(7, 5, 4, 'H1', 'Unitree H1', 'H1', 'unitree-h1',
 '宇树H1是高性能通用人形机器人，能完成快速行走（3.3m/s）、跑步、跳跃等动作，是目前商业化人形机器人中运动性能最强的产品之一。',
 2023, 'active',
 47.00, 1800, 3.300, 2.0,
 19,
 'premium', 'https://via.placeholder.com/800x600?text=Unitree+H1', 1, 1, 92,
 '{"max_running_speed": "3.3m/s", "jumping": true, "compute": "NVIDIA Jetson Orin NX", "force_control": true}');

-- 8. 优必选 Yanshee 教育机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, height_mm, walking_speed_m_s,
    dof,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(8, 4, 8, 'Yanshee', 'UBTECH Yanshee', 'Yanshee', 'ubtech-yanshee',
 '优必选Yanshee是面向青少年STEAM教育的人形机器人，支持图形化编程和Python编程，内置多种传感器，适合中学和大学教学。',
 2018, 'active',
 1.50, 388, 0.100,
 16,
 'low', 'https://via.placeholder.com/800x600?text=UBTECH+Yanshee', 0, 1, 70,
 '{"programming": ["Scratch", "Python", "ROS"], "sensors": ["camera", "microphone", "gyro", "ultrasonic"], "app_control": true}');

-- 9. FANUC CRX-10iA 协作机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    dof, payload_kg, reach_mm, repeatability_mm, weight_kg,
    ip_rating,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(9, 3, 2, 'CRX-10iA', 'FANUC CRX-10iA', 'CRX-10iA/L', 'fanuc-crx-10ia',
 'FANUC CRX-10iA是发那科新一代协作机器人，采用绿色外观设计，支持手动引导示教，无需专业编程知识即可快速部署，适合中小企业柔性生产线。',
 2020, 'active',
 6, 10.000, 1418, 0.0500, 55.00,
 'IP67',
 'medium', 'https://via.placeholder.com/800x600?text=FANUC+CRX-10iA', 1, 1, 82,
 '{"safety_standard": "ISO 10218-1", "hand_guiding": true, "no_safety_fence": true, "teach_pendant": "tablet-style"}');

-- 10. 波士顿动力 Atlas 人形机器人
INSERT IGNORE INTO robots (id, manufacturer_id, category_id, name, name_en, model_number, slug,
    description, release_year, status,
    weight_kg, height_mm, walking_speed_m_s, dof,
    price_range, cover_image_url, is_featured, is_verified, sort_order,
    extra_specs) VALUES
(10, 1, 4, 'Atlas', 'Boston Dynamics Atlas', 'Atlas 2024', 'boston-dynamics-atlas',
 '波士顿动力Atlas是全球最先进的人形机器人，能完成后空翻、奔跑、跳跃等高难度动作。最新电动版本采用全新液压和电机系统，正在向工业应用场景推进。',
 2024, 'active',
 89.00, 1500, 2.500, 28,
 'inquiry', 'https://via.placeholder.com/800x600?text=Boston+Dynamics+Atlas', 1, 1, 98,
 '{"backflip": true, "running": true, "hydraulic_electric": "electric", "manipulation": "advanced", "perception": "stereo+depth+lidar"}');

-- =============================================
-- 机器人应用领域关联
-- =============================================
INSERT IGNORE INTO robot_application_domains (robot_id, domain_id) VALUES
-- ABB IRB 6700: 汽车制造, 金属加工
(1, 1), (1, 7),
-- FANUC R-2000iC: 汽车制造, 金属加工, 航空航天
(2, 1), (2, 7), (2, 6),
-- ABB YuMi: 电子/半导体, 3C消费电子
(3, 2), (3, 8),
-- Spot: 物流仓储, 航空航天
(4, 3), (4, 6),
-- Unitree Go2: 物流仓储, 农业
(5, 3), (5, 10),
-- Walker X: 医疗健康, 餐饮服务
(6, 5), (6, 9),
-- Unitree H1: 物流仓储, 汽车制造
(7, 3), (7, 1),
-- Yanshee: 医疗健康
(8, 5),
-- FANUC CRX-10iA: 电子/半导体, 食品饮料, 3C消费电子
(9, 2), (9, 4), (9, 8),
-- Atlas: 汽车制造, 物流仓储
(10, 1), (10, 3);

-- =============================================
-- 管理员账号
-- 邮箱: admin@lookforbest.com
-- 密码: Admin@123456
-- BCrypt(10): $2y$10$/Sx9XuHyXIzDkE5g6nVk9u/7Og4/kl.gkqbRcU4.7wQZ0wPjSss1m
-- =============================================
INSERT IGNORE INTO users (id, email, username, password_hash, display_name, role, is_active, email_verified) VALUES
(1, 'admin@lookforbest.com', 'admin', '$2y$10$/Sx9XuHyXIzDkE5g6nVk9u/7Og4/kl.gkqbRcU4.7wQZ0wPjSss1m',
 'LookForBest管理员', 'superadmin', 1, 1);
