-- 价格历史记录表
CREATE TABLE IF NOT EXISTS robot_price_history (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    robot_id    BIGINT         NOT NULL COMMENT '机器人ID',
    price       DECIMAL(12, 2) NOT NULL COMMENT '价格',
    currency    VARCHAR(10)    NOT NULL DEFAULT 'CNY',
    source      VARCHAR(100) COMMENT '数据来源',
    recorded_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_robot_id_recorded (robot_id, recorded_at)
) COMMENT = '机器人价格历史';

-- 降价提醒表
CREATE TABLE IF NOT EXISTS price_alerts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT         NOT NULL COMMENT '用户ID',
    robot_id     BIGINT         NOT NULL COMMENT '机器人ID',
    target_price DECIMAL(12, 2) NOT NULL COMMENT '目标价格（低于此价格触发提醒）',
    is_triggered TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否已触发',
    triggered_at DATETIME       NULL COMMENT '触发时间',
    is_active    TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '是否有效',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_robot (user_id, robot_id),
    INDEX idx_robot_id (robot_id),
    INDEX idx_user_id (user_id)
) COMMENT = '降价提醒设置';
