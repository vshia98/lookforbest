package com.lookforbest.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotEsDocument;
import com.lookforbest.entity.SearchHotKeyword;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.repository.SearchHotKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchHotKeywordRepository hotKeywordRepository;
    private final RobotRepository robotRepository;

    @Value("${app.elasticsearch.enabled:false}")
    private boolean esEnabled;

    /**
     * 全文搜索机器人 — 优先走 ES，ES 不可用时降级到 MySQL
     */
    public Map<String, Object> searchRobots(
            String q, Long categoryId, Long manufacturerId,
            BigDecimal payloadMin, BigDecimal payloadMax,
            Integer reachMin, Integer reachMax,
            Integer dofMin, Integer dofMax,
            Boolean has3dModel, String status,
            String sort, Pageable pageable) {

        // 记录热词
        if (StringUtils.hasText(q)) {
            recordKeyword(q.trim());
        }

        try {
            return searchViaElasticsearch(q, categoryId, manufacturerId,
                    payloadMin, payloadMax, reachMin, reachMax,
                    dofMin, dofMax, has3dModel, status, sort, pageable);
        } catch (Exception e) {
            log.warn("Elasticsearch 不可用，降级到数据库搜索: {}", e.getMessage());
            return searchViaDatabase(q, categoryId, manufacturerId,
                    payloadMin, payloadMax, reachMin, reachMax,
                    dofMin, dofMax, has3dModel, status, sort, pageable);
        }
    }

    /** ES 搜索 */
    private Map<String, Object> searchViaElasticsearch(
            String q, Long categoryId, Long manufacturerId,
            BigDecimal payloadMin, BigDecimal payloadMax,
            Integer reachMin, Integer reachMax,
            Integer dofMin, Integer dofMax,
            Boolean has3dModel, String status,
            String sort, Pageable pageable) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 全文搜索
        if (StringUtils.hasText(q)) {
            boolQuery.must(MultiMatchQuery.of(mm -> mm
                    .query(q)
                    .fields(
                            "name^4",
                            "nameEn^3",
                            "manufacturerName^2",
                            "modelNumber^3",
                            "description^1",
                            "descriptionEn^1"
                    )
                    .type(TextQueryType.BestFields)
                    .fuzziness("AUTO")
            )._toQuery());
        }

        // 过滤条件
        if (categoryId != null) {
            boolQuery.filter(TermQuery.of(t -> t.field("categoryId").value(categoryId))._toQuery());
        }
        if (manufacturerId != null) {
            boolQuery.filter(TermQuery.of(t -> t.field("manufacturerId").value(manufacturerId))._toQuery());
        }
        if (StringUtils.hasText(status)) {
            boolQuery.filter(TermQuery.of(t -> t.field("status").value(status))._toQuery());
        }
        if (has3dModel != null) {
            boolQuery.filter(TermQuery.of(t -> t.field("has3dModel").value(has3dModel))._toQuery());
        }
        if (payloadMin != null || payloadMax != null) {
            RangeQuery.Builder range = new RangeQuery.Builder().field("payloadKg");
            if (payloadMin != null) range.gte(co.elastic.clients.json.JsonData.of(payloadMin));
            if (payloadMax != null) range.lte(co.elastic.clients.json.JsonData.of(payloadMax));
            boolQuery.filter(range.build()._toQuery());
        }
        if (reachMin != null || reachMax != null) {
            RangeQuery.Builder range = new RangeQuery.Builder().field("reachMm");
            if (reachMin != null) range.gte(co.elastic.clients.json.JsonData.of(reachMin));
            if (reachMax != null) range.lte(co.elastic.clients.json.JsonData.of(reachMax));
            boolQuery.filter(range.build()._toQuery());
        }
        if (dofMin != null || dofMax != null) {
            RangeQuery.Builder range = new RangeQuery.Builder().field("dof");
            if (dofMin != null) range.gte(co.elastic.clients.json.JsonData.of(dofMin));
            if (dofMax != null) range.lte(co.elastic.clients.json.JsonData.of(dofMax));
            boolQuery.filter(range.build()._toQuery());
        }

        // 高亮配置
        List<HighlightField> highlightFields = List.of(
                new HighlightField("name"),
                new HighlightField("nameEn"),
                new HighlightField("description"),
                new HighlightField("manufacturerName")
        );
        HighlightParameters highlightParams = HighlightParameters.builder()
                .withPreTags("<em class=\"highlight\">")
                .withPostTags("</em>")
                .withNumberOfFragments(3)
                .withFragmentSize(150)
                .build();
        Highlight highlight = new Highlight(highlightParams, highlightFields);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery.build()._toQuery())
                .withHighlightQuery(new org.springframework.data.elasticsearch.core.query.HighlightQuery(highlight, RobotEsDocument.class))
                .withPageable(pageable)
                .build();

        SearchHits<RobotEsDocument> hits = elasticsearchOperations.search(nativeQuery, RobotEsDocument.class);

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit<RobotEsDocument> hit : hits.getSearchHits()) {
            RobotEsDocument doc = hit.getContent();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", doc.getId());
            item.put("name", doc.getName());
            item.put("nameEn", doc.getNameEn());
            item.put("slug", doc.getSlug());
            item.put("modelNumber", doc.getModelNumber());
            item.put("categoryId", doc.getCategoryId());
            item.put("categoryName", doc.getCategoryName());
            item.put("manufacturerId", doc.getManufacturerId());
            item.put("manufacturerName", doc.getManufacturerName());
            item.put("coverImageUrl", doc.getCoverImageUrl());
            item.put("priceRange", doc.getPriceRange());
            item.put("has3dModel", doc.getHas3dModel());
            item.put("viewCount", doc.getViewCount());
            item.put("isFeatured", doc.getIsFeatured());
            // 高亮片段
            Map<String, List<String>> highlightMap = hit.getHighlightFields();
            if (!highlightMap.isEmpty()) {
                item.put("highlights", highlightMap);
            }
            results.add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hits", results);
        response.put("total", hits.getTotalHits());
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("totalPages", (int) Math.ceil((double) hits.getTotalHits() / pageable.getPageSize()));
        return response;
    }

    /** 去除搜索词中的通用噪音词 */
    private static final Set<String> STOP_WORDS = Set.of(
            "机器人", "机器", "产品", "设备", "系统", "智能", "方案", "的", "了", "和", "是"
    );

    private String cleanQuery(String q) {
        if (q == null) return null;
        String cleaned = q.trim();
        for (String sw : STOP_WORDS) {
            cleaned = cleaned.replace(sw, " ");
        }
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    /** 数据库降级搜索 — 拆词 + 去噪音词，多关键词合并结果 */
    private Map<String, Object> searchViaDatabase(
            String q, Long categoryId, Long manufacturerId,
            BigDecimal payloadMin, BigDecimal payloadMax,
            Integer reachMin, Integer reachMax,
            Integer dofMin, Integer dofMax,
            Boolean has3dModel, String status,
            String sort, Pageable pageable) {

        Robot.RobotStatus robotStatus = null;
        if (StringUtils.hasText(status)) {
            try { robotStatus = Robot.RobotStatus.valueOf(status); } catch (IllegalArgumentException ignored) {}
        }

        // 清理搜索词并拆成关键词列表
        String cleaned = cleanQuery(q);
        List<String> keywords = new ArrayList<>();
        if (StringUtils.hasText(cleaned)) {
            // 原始清理后的词（如 "轮式"）
            keywords.add(cleaned);
            // 按空格拆分的子词
            for (String part : cleaned.split("\\s+")) {
                if (part.length() >= 2 && !keywords.contains(part)) {
                    keywords.add(part);
                }
            }
        }

        // 用所有关键词分别查询，合并去重
        LinkedHashMap<Long, Robot> merged = new LinkedHashMap<>();
        long totalHits = 0;
        for (String kw : keywords) {
            Page<Robot> page = robotRepository.findWithFilters(
                    kw, categoryId, manufacturerId, null,
                    payloadMin, payloadMax, reachMin, reachMax,
                    dofMin, dofMax, has3dModel, robotStatus, pageable
            );
            for (Robot r : page.getContent()) {
                merged.putIfAbsent(r.getId(), r);
            }
            totalHits = Math.max(totalHits, page.getTotalElements());
        }

        // 如果关键词为空（无搜索词），走原始查询
        if (keywords.isEmpty()) {
            Page<Robot> page = robotRepository.findWithFilters(
                    null, categoryId, manufacturerId, null,
                    payloadMin, payloadMax, reachMin, reachMax,
                    dofMin, dofMax, has3dModel, robotStatus, pageable
            );
            for (Robot r : page.getContent()) {
                merged.putIfAbsent(r.getId(), r);
            }
            totalHits = page.getTotalElements();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        int skip = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();
        int idx = 0;
        for (Robot r : merged.values()) {
            if (idx >= skip && results.size() < limit) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", r.getId());
                item.put("name", r.getName());
                item.put("nameEn", r.getNameEn());
                item.put("slug", r.getSlug());
                item.put("modelNumber", r.getModelNumber());
                item.put("categoryId", r.getCategory() != null ? r.getCategory().getId() : null);
                item.put("categoryName", r.getCategory() != null ? r.getCategory().getName() : null);
                item.put("manufacturerId", r.getManufacturer() != null ? r.getManufacturer().getId() : null);
                item.put("manufacturerName", r.getManufacturer() != null ? r.getManufacturer().getName() : null);
                item.put("coverImageUrl", r.getCoverImageUrl());
                item.put("priceRange", r.getPriceRange());
                item.put("has3dModel", r.getHas3dModel());
                item.put("viewCount", r.getViewCount());
                item.put("isFeatured", r.getIsFeatured());
                results.add(item);
            }
            idx++;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hits", results);
        response.put("total", Math.max(totalHits, merged.size()));
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("totalPages", (int) Math.ceil((double) Math.max(totalHits, merged.size()) / pageable.getPageSize()));
        return response;
    }

    /**
     * 搜索联想/自动补全（基于 multi_match prefix 查询，返回机器人名称建议）
     */
    public List<String> suggest(String prefix) {
        if (!StringUtils.hasText(prefix)) return List.of();
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder()
                    .should(MatchPhrasePrefixQuery.of(m -> m.field("name").query(prefix).maxExpansions(10))._toQuery())
                    .should(MatchPhrasePrefixQuery.of(m -> m.field("nameEn").query(prefix).maxExpansions(10))._toQuery())
                    .should(MatchPhrasePrefixQuery.of(m -> m.field("manufacturerName").query(prefix).maxExpansions(10))._toQuery())
                    .minimumShouldMatch("1");

            NativeQuery query = NativeQuery.builder()
                    .withQuery(boolQuery.build()._toQuery())
                    .withPageable(org.springframework.data.domain.PageRequest.of(0, 8))
                    .build();

            SearchHits<RobotEsDocument> hits = elasticsearchOperations.search(query, RobotEsDocument.class);
            Set<String> seen = new LinkedHashSet<>();
            for (SearchHit<RobotEsDocument> hit : hits.getSearchHits()) {
                RobotEsDocument doc = hit.getContent();
                if (doc.getName() != null && doc.getName().toLowerCase().contains(prefix.toLowerCase())) {
                    seen.add(doc.getName());
                } else if (doc.getNameEn() != null && doc.getNameEn().toLowerCase().contains(prefix.toLowerCase())) {
                    seen.add(doc.getNameEn());
                } else if (doc.getManufacturerName() != null && doc.getManufacturerName().toLowerCase().contains(prefix.toLowerCase())) {
                    seen.add(doc.getManufacturerName());
                } else if (doc.getName() != null) {
                    seen.add(doc.getName());
                }
            }
            return new ArrayList<>(seen);
        } catch (Exception e) {
            log.warn("联想查询失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 获取热搜词 Top 10
     */
    public List<String> getHotKeywords() {
        return hotKeywordRepository.findTop10ByOrderBySearchCountDesc()
                .stream()
                .map(SearchHotKeyword::getKeyword)
                .toList();
    }

    @Transactional
    protected void recordKeyword(String keyword) {
        try {
            hotKeywordRepository.upsertKeyword(keyword);
        } catch (Exception e) {
            log.warn("记录搜索热词失败: {}", e.getMessage());
        }
    }
}
