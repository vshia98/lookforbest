package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.service.ElasticsearchSyncService;
import com.lookforbest.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "搜索", description = "Elasticsearch 全文搜索接口")
public class SearchController {

    private final SearchService searchService;
    private final ElasticsearchSyncService syncService;

    @GetMapping
    @Operation(summary = "全文搜索机器人（ES）", description = "支持多字段加权、中文分词、模糊匹配与高亮")
    public ApiResponse<?> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false) BigDecimal payloadMin,
            @RequestParam(required = false) BigDecimal payloadMax,
            @RequestParam(required = false) Integer reachMin,
            @RequestParam(required = false) Integer reachMax,
            @RequestParam(required = false) Integer dofMin,
            @RequestParam(required = false) Integer dofMax,
            @RequestParam(required = false) Boolean has3dModel,
            @RequestParam(defaultValue = "active") String status,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        size = Math.min(size, 100);
        Map<String, Object> result = searchService.searchRobots(
                q, categoryId, manufacturerId,
                payloadMin, payloadMax,
                reachMin, reachMax,
                dofMin, dofMax,
                has3dModel, status, sort,
                PageRequest.of(page, size)
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/suggest")
    @Operation(summary = "搜索联想/自动补全", description = "基于 ES completion suggester，支持中文联想")
    public ApiResponse<List<String>> suggest(@RequestParam String q) {
        return ApiResponse.ok(searchService.suggest(q));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取搜索热词 Top10")
    public ApiResponse<List<String>> hotKeywords() {
        return ApiResponse.ok(searchService.getHotKeywords());
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "触发全量同步（管理员）", description = "将 MySQL 机器人数据全量同步到 Elasticsearch")
    public ApiResponse<String> fullSync() {
        syncService.fullSync();
        return ApiResponse.ok("全量同步已触发");
    }
}
