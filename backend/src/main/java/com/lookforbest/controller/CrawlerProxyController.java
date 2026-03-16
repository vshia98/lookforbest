package com.lookforbest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 爬虫服务代理接口（管理员使用）
 * 将 /api/v1/admin/crawler/** 请求转发至爬虫微服务
 */
@RestController
@RequestMapping("/api/v1/admin/crawler")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
@Slf4j
@Tag(name = "爬虫管理", description = "配置化爬虫数据源管理与监控（管理员）")
public class CrawlerProxyController {

    @Value("${app.crawler.url:http://localhost:8002}")
    private String crawlerUrl;

    private final RestTemplate restTemplate;

    private ResponseEntity<Object> proxy(HttpMethod method, String path, Object body) {
        String url = crawlerUrl + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            return restTemplate.exchange(url, method, entity, Object.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("爬虫服务请求失败: {} {}: {}", method, path, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "爬虫服务不可用: " + e.getMessage()));
        }
    }

    @GetMapping("/sources")
    @Operation(summary = "获取所有爬虫数据源")
    public ResponseEntity<Object> listSources() {
        return proxy(HttpMethod.GET, "/api/sources", null);
    }

    @GetMapping("/sources/{id}")
    @Operation(summary = "获取单个数据源详情")
    public ResponseEntity<Object> getSource(@PathVariable Long id) {
        return proxy(HttpMethod.GET, "/api/sources/" + id, null);
    }

    @PostMapping("/sources")
    @Operation(summary = "创建爬虫数据源")
    public ResponseEntity<Object> createSource(@RequestBody Map<String, Object> body) {
        return proxy(HttpMethod.POST, "/api/sources", body);
    }

    @PutMapping("/sources/{id}")
    @Operation(summary = "更新爬虫数据源")
    public ResponseEntity<Object> updateSource(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return proxy(HttpMethod.PUT, "/api/sources/" + id, body);
    }

    @DeleteMapping("/sources/{id}")
    @Operation(summary = "删除爬虫数据源")
    public ResponseEntity<Object> deleteSource(@PathVariable Long id) {
        return proxy(HttpMethod.DELETE, "/api/sources/" + id, null);
    }

    @PostMapping("/sources/{id}/run")
    @Operation(summary = "手动触发单个数据源爬取")
    public ResponseEntity<Object> runSource(@PathVariable Long id) {
        return proxy(HttpMethod.POST, "/api/sources/" + id + "/run", null);
    }

    @PostMapping("/run-all")
    @Operation(summary = "触发所有活跃数据源爬取")
    public ResponseEntity<Object> runAll() {
        return proxy(HttpMethod.POST, "/api/run-all", null);
    }

    @GetMapping("/logs")
    @Operation(summary = "获取爬取运行日志")
    public ResponseEntity<Object> getLogs(
            @RequestParam(required = false) Long sourceId,
            @RequestParam(defaultValue = "50") int limit) {
        String path = "/api/logs?limit=" + limit;
        if (sourceId != null) path += "&source_id=" + sourceId;
        return proxy(HttpMethod.GET, path, null);
    }

    @GetMapping("/status")
    @Operation(summary = "获取爬虫运行状态")
    public ResponseEntity<Object> getStatus() {
        return proxy(HttpMethod.GET, "/api/status", null);
    }

    @PostMapping("/preview")
    @Operation(summary = "预览选择器提取结果")
    public ResponseEntity<Object> preview(@RequestBody Map<String, Object> body) {
        return proxy(HttpMethod.POST, "/api/preview", body);
    }

    @PostMapping("/fetch-page")
    @Operation(summary = "抓取页面原始HTML（可视化选择器工具）")
    public ResponseEntity<Object> fetchPage(@RequestBody Map<String, Object> body) {
        return proxy(HttpMethod.POST, "/api/fetch-page", body);
    }
}
