package com.lookforbest.service;

import com.lookforbest.entity.PriceAlert;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotPriceHistory;
import com.lookforbest.repository.PriceAlertRepository;
import com.lookforbest.repository.RobotPriceHistoryRepository;
import com.lookforbest.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceTrackingService {

    private final RobotPriceHistoryRepository priceHistoryRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final RobotRepository robotRepository;
    private final NotificationService notificationService;

    /** 获取机器人价格历史（近90天） */
    public List<Map<String, Object>> getPriceHistory(Long robotId) {
        LocalDateTime since = LocalDateTime.now().minusDays(90);
        return priceHistoryRepository
                .findByRobotIdAndRecordedAtAfterOrderByRecordedAtAsc(robotId, since)
                .stream()
                .map(h -> Map.<String, Object>of(
                        "price", h.getPrice(),
                        "currency", h.getCurrency(),
                        "recordedAt", h.getRecordedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    /** 设置/更新降价提醒 */
    @Transactional
    public PriceAlert setAlert(Long userId, Long robotId, BigDecimal targetPrice) {
        PriceAlert alert = priceAlertRepository.findByUserIdAndRobotId(userId, robotId)
                .orElseGet(() -> {
                    PriceAlert a = new PriceAlert();
                    a.setUserId(userId);
                    a.setRobotId(robotId);
                    return a;
                });
        alert.setTargetPrice(targetPrice);
        alert.setTriggered(false);
        alert.setActive(true);
        alert.setTriggeredAt(null);
        return priceAlertRepository.save(alert);
    }

    /** 取消降价提醒 */
    @Transactional
    public void cancelAlert(Long userId, Long robotId) {
        priceAlertRepository.findByUserIdAndRobotId(userId, robotId).ifPresent(a -> {
            a.setActive(false);
            priceAlertRepository.save(a);
        });
    }

    /** 获取用户所有提醒 */
    public List<PriceAlert> getUserAlerts(Long userId) {
        return priceAlertRepository.findByUserId(userId);
    }

    /** 记录价格快照（爬虫或手动更新时调用） */
    @Transactional
    public void recordPrice(Long robotId, BigDecimal price, String source) {
        RobotPriceHistory history = new RobotPriceHistory();
        history.setRobotId(robotId);
        history.setPrice(price);
        history.setSource(source != null ? source : "manual");
        priceHistoryRepository.save(history);

        // 检查并触发降价提醒
        checkAndTriggerAlerts(robotId, price);
    }

    /** 定时任务：每日同步机器人当前价格到历史表 */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void dailyPriceSnapshot() {
        log.info("开始每日价格快照...");
        List<Robot> robots = robotRepository.findAll();
        int count = 0;
        for (Robot robot : robots) {
            if (robot.getPriceUsdFrom() != null) {
                RobotPriceHistory history = new RobotPriceHistory();
                history.setRobotId(robot.getId());
                history.setPrice(robot.getPriceUsdFrom());
                history.setSource("daily_snapshot");
                priceHistoryRepository.save(history);
                checkAndTriggerAlerts(robot.getId(), robot.getPriceUsdFrom());
                count++;
            }
        }
        log.info("价格快照完成，共记录 {} 条", count);
    }

    private void checkAndTriggerAlerts(Long robotId, BigDecimal currentPrice) {
        List<PriceAlert> alerts = priceAlertRepository.findByRobotIdAndActiveTrueAndTriggeredFalse(robotId);
        for (PriceAlert alert : alerts) {
            if (currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                alert.setTriggered(true);
                alert.setTriggeredAt(LocalDateTime.now());
                priceAlertRepository.save(alert);

                // 发送通知
                robotRepository.findById(robotId).ifPresent(robot ->
                        notificationService.sendPriceAlert(alert.getUserId(), robot, currentPrice)
                );
                log.info("触发降价提醒: userId={}, robotId={}, price={}", alert.getUserId(), robotId, currentPrice);
            }
        }
    }
}
