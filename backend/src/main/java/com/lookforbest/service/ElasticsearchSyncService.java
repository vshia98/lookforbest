package com.lookforbest.service;

import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotEsDocument;
import com.lookforbest.repository.es.RobotEsRepository;
import com.lookforbest.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 负责将 MySQL 机器人数据同步到 Elasticsearch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncService {

    private final RobotRepository robotRepository;
    private final RobotEsRepository robotEsRepository;

    @Value("${app.elasticsearch.enabled:false}")
    private boolean esEnabled;

    /**
     * 全量同步：将所有机器人数据写入 ES
     */
    @Transactional(readOnly = true)
    public void fullSync() {
        if (!esEnabled) return;
        log.info("开始全量同步机器人数据到 Elasticsearch...");
        int page = 0;
        int batchSize = 200;
        int total = 0;
        while (true) {
            List<Robot> robots = robotRepository.findAll(
                    PageRequest.of(page, batchSize)).getContent();
            if (robots.isEmpty()) break;
            List<RobotEsDocument> docs = robots.stream()
                    .map(RobotEsDocument::from)
                    .toList();
            robotEsRepository.saveAll(docs);
            total += docs.size();
            page++;
        }
        log.info("全量同步完成，共同步 {} 条记录", total);
    }

    /**
     * 增量同步：同步单个机器人到 ES（在机器人创建/更新时调用）
     */
    @Async
    public void syncRobot(Robot robot) {
        if (!esEnabled) return;
        try {
            robotEsRepository.save(RobotEsDocument.from(robot));
        } catch (Exception e) {
            log.warn("同步机器人 {} 到 ES 失败: {}", robot.getId(), e.getMessage());
        }
    }

    /**
     * 从 ES 删除机器人文档
     */
    @Async
    public void deleteRobot(Long robotId) {
        if (!esEnabled) return;
        try {
            robotEsRepository.deleteById(String.valueOf(robotId));
        } catch (Exception e) {
            log.warn("从 ES 删除机器人 {} 失败: {}", robotId, e.getMessage());
        }
    }

    /**
     * 定时全量同步（每天凌晨 2 点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledFullSync() {
        fullSync();
    }
}
