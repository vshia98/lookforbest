package com.lookforbest.service;

import com.lookforbest.dto.response.RobotSummaryDTO;
import com.lookforbest.entity.Robot;
import com.lookforbest.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RobotRepository robotRepository;

    @Value("${app.recommend.url:http://localhost:8000}")
    private String recommendUrl;

    @Value("${app.recommend.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取相似机器人推荐
     * 优先调用 Python 推荐服务，失败则降级到分类+热门度
     */
    public List<RobotSummaryDTO> getSimilar(Long robotId, int size, String strategy) {
        if (enabled) {
            try {
                return callPythonService(robotId, size, strategy);
            } catch (Exception e) {
                log.warn("推荐服务调用失败，降级到本地推荐: {}", e.getMessage());
            }
        }
        return fallback(robotId, size);
    }

    /** 调用 Python FastAPI 推荐服务 */
    private List<RobotSummaryDTO> callPythonService(Long robotId, int size, String strategy) {
        Map<String, Object> body = new HashMap<>();
        body.put("robotId", robotId);
        body.put("size", size);
        body.put("strategy", strategy != null ? strategy : "hybrid");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                recommendUrl + "/recommend/similar",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("推荐服务返回非 2xx");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        if (items == null || items.isEmpty()) return Collections.emptyList();

        List<Long> robotIds = items.stream()
                .map(m -> Long.valueOf(m.get("robotId").toString()))
                .toList();

        List<Robot> robots = robotRepository.findAllById(robotIds);
        Map<Long, Robot> robotMap = new HashMap<>();
        robots.forEach(r -> robotMap.put(r.getId(), r));

        return robotIds.stream()
                .map(robotMap::get)
                .filter(Objects::nonNull)
                .map(RobotSummaryDTO::from)
                .toList();
    }

    /** 本地降级：同分类按浏览量排序 */
    private List<RobotSummaryDTO> fallback(Long robotId, int size) {
        return robotRepository.findById(robotId)
                .map(robot -> robotRepository
                        .findTop6ByCategoryAndIdNotOrderByViewCountDesc(robot.getCategory(), robotId)
                        .stream()
                        .limit(size)
                        .map(RobotSummaryDTO::from)
                        .toList())
                .orElse(Collections.emptyList());
    }
}
