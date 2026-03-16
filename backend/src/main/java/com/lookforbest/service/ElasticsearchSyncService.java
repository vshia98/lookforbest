package com.lookforbest.service;

import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotEsDocument;
import com.lookforbest.repository.es.RobotEsRepository;
import com.lookforbest.repository.RobotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class ElasticsearchSyncService {

    private final RobotRepository robotRepository;

    @Autowired(required = false)
    private RobotEsRepository robotEsRepository;

    @Value("${app.elasticsearch.enabled:false}")
    private boolean esEnabled;

    public ElasticsearchSyncService(RobotRepository robotRepository) {
        this.robotRepository = robotRepository;
    }

    @Transactional(readOnly = true)
    public void fullSync() {
        if (!esEnabled || robotEsRepository == null) return;
        log.info("开始全量同步机器人数据到 Elasticsearch...");
        int page = 0;
        int batchSize = 200;
        int total = 0;
        while (true) {
            List<Robot> robots = robotRepository.findAll(PageRequest.of(page, batchSize)).getContent();
            if (robots.isEmpty()) break;
            List<RobotEsDocument> docs = robots.stream().map(RobotEsDocument::from).toList();
            robotEsRepository.saveAll(docs);
            total += docs.size();
            page++;
        }
        log.info("全量同步完成，共同步 {} 条记录", total);
    }

    @Async
    public void syncRobot(Robot robot) {
        if (!esEnabled || robotEsRepository == null) return;
        try {
            robotEsRepository.save(RobotEsDocument.from(robot));
        } catch (Exception e) {
            log.warn("同步机器人 {} 到 ES 失败: {}", robot.getId(), e.getMessage());
        }
    }

    @Async
    public void deleteRobot(Long robotId) {
        if (!esEnabled || robotEsRepository == null) return;
        try {
            robotEsRepository.deleteById(String.valueOf(robotId));
        } catch (Exception e) {
            log.warn("从 ES 删除机器人 {} 失败: {}", robotId, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledFullSync() {
        fullSync();
    }
}
