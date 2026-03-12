package com.lookforbest.controller;

import com.lookforbest.dto.request.AdCreateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.MembershipPlanDTO;
import com.lookforbest.dto.response.OrderDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.AdPlacement;
import com.lookforbest.entity.CaseStudy;
import com.lookforbest.entity.ManufacturerApplication;
import com.lookforbest.entity.MembershipPlan;
import com.lookforbest.entity.PaymentOrder;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotReview;
import com.lookforbest.entity.User;
import com.lookforbest.repository.ManufacturerRepository;
import com.lookforbest.repository.MembershipPlanRepository;
import com.lookforbest.repository.RobotCategoryRepository;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.AdService;
import com.lookforbest.service.ManufacturerPortalService;
import com.lookforbest.service.MembershipService;
import com.lookforbest.service.UgcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class AdminController {

    private final RobotRepository robotRepository;
    private final UserRepository userRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final RobotCategoryRepository categoryRepository;
    private final UgcService ugcService;
    private final AdService adService;
    private final ManufacturerPortalService manufacturerPortalService;
    private final MembershipService membershipService;
    private final MembershipPlanRepository membershipPlanRepository;

    /** 统计仪表盘数据 */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 机器人总数及状态分布
        long totalRobots = robotRepository.count();
        long activeRobots = robotRepository.countByStatus(Robot.RobotStatus.active);
        long discontinuedRobots = robotRepository.countByStatus(Robot.RobotStatus.discontinued);
        long upcomingRobots = robotRepository.countByStatus(Robot.RobotStatus.upcoming);

        stats.put("totalRobots", totalRobots);
        stats.put("activeRobots", activeRobots);
        stats.put("discontinuedRobots", discontinuedRobots);
        stats.put("upcomingRobots", upcomingRobots);

        // 厂商 & 用户 & 分类数量
        stats.put("totalManufacturers", manufacturerRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCategories", categoryRepository.count());

        // 总浏览量
        Long totalViews = robotRepository.sumViewCount();
        stats.put("totalViews", totalViews != null ? totalViews : 0L);

        // 总收藏量
        Long totalFavorites = robotRepository.sumFavoriteCount();
        stats.put("totalFavorites", totalFavorites != null ? totalFavorites : 0L);

        // 分类分布（Top 10）
        List<Map<String, Object>> categoryDistribution = robotRepository.countByCategory();
        stats.put("categoryDistribution", categoryDistribution);

        // 近期活跃机器人（按浏览量 Top 10）
        List<Map<String, Object>> topRobots = robotRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId());
                    m.put("name", r.getName());
                    m.put("viewCount", r.getViewCount());
                    m.put("favoriteCount", r.getFavoriteCount());
                    m.put("status", r.getStatus());
                    return m;
                })
                .collect(Collectors.toList());
        stats.put("topRobots", topRobots);

        return ApiResponse.ok(stats);
    }

    /** 批量删除机器人 */
    @DeleteMapping("/robots/batch")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error(400, "ids不能为空");
        }
        robotRepository.deleteAllById(ids);
        return ApiResponse.ok();
    }

    /** 批量修改机器人状态 */
    @PatchMapping("/robots/batch/status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        String statusStr = (String) body.get("status");

        if (rawIds == null || rawIds.isEmpty() || statusStr == null) {
            return ApiResponse.error(400, "ids和status不能为空");
        }

        Robot.RobotStatus status;
        try {
            status = Robot.RobotStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "无效状态值: " + statusStr);
        }

        List<Long> ids = rawIds.stream().map(Integer::longValue).collect(Collectors.toList());
        List<Robot> robots = robotRepository.findAllById(ids);
        robots.forEach(r -> r.setStatus(status));
        robotRepository.saveAll(robots);
        return ApiResponse.ok();
    }

    /** 评测审核列表 */
    @GetMapping("/reviews")
    public ApiResponse<?> listReviews(
            @RequestParam(defaultValue = "pending_review") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        RobotReview.Status reviewStatus = RobotReview.Status.valueOf(status);
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.ASC, "createdAt"));
        return ApiResponse.ok(PagedResponse.of(ugcService.getReviewsByStatus(reviewStatus, pageable)));
    }

    /** 发布评测 */
    @PatchMapping("/reviews/{id}/publish")
    public ApiResponse<Void> publishReview(@PathVariable Long id) {
        ugcService.publishReview(id);
        return ApiResponse.ok();
    }

    /** 拒绝评测 */
    @PatchMapping("/reviews/{id}/reject")
    public ApiResponse<Void> rejectReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ugcService.rejectReview(id, body.getOrDefault("reason", ""));
        return ApiResponse.ok();
    }

    /** 案例审核列表 */
    @GetMapping("/case-studies")
    public ApiResponse<?> listCaseStudies(
            @RequestParam(defaultValue = "pending_review") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        CaseStudy.Status csStatus = CaseStudy.Status.valueOf(status);
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.ASC, "createdAt"));
        return ApiResponse.ok(PagedResponse.of(ugcService.getCaseStudiesByStatus(csStatus, pageable)));
    }

    /** 发布案例 */
    @PatchMapping("/case-studies/{id}/publish")
    public ApiResponse<Void> publishCaseStudy(@PathVariable Long id) {
        ugcService.publishCaseStudy(id);
        return ApiResponse.ok();
    }

    /** 拒绝案例 */
    @PatchMapping("/case-studies/{id}/reject")
    public ApiResponse<Void> rejectCaseStudy(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ugcService.rejectCaseStudy(id, body.getOrDefault("reason", ""));
        return ApiResponse.ok();
    }

    // ==================== 广告管理 ====================

    /** 查询所有广告 */
    @GetMapping("/ads")
    public ApiResponse<List<AdPlacement>> listAds() {
        return ApiResponse.ok(adService.listAllAds());
    }

    /** 创建广告 */
    @PostMapping("/ads")
    public ApiResponse<AdPlacement> createAd(@Valid @RequestBody AdCreateRequest req) {
        return ApiResponse.ok(adService.createAd(req));
    }

    /** 更新广告 */
    @PutMapping("/ads/{id}")
    public ApiResponse<AdPlacement> updateAd(@PathVariable Long id, @Valid @RequestBody AdCreateRequest req) {
        return ApiResponse.ok(adService.updateAd(id, req));
    }

    /** 删除广告 */
    @DeleteMapping("/ads/{id}")
    public ApiResponse<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ApiResponse.ok();
    }

    /** CTR 报告 */
    @GetMapping("/ads/ctr-report")
    public ApiResponse<List<Map<String, Object>>> getCtrReport() {
        return ApiResponse.ok(adService.getCtrReport());
    }

    // ==================== 厂商申请管理 ====================

    /** 查询厂商入驻申请列表 */
    @GetMapping("/manufacturer-applications")
    public ApiResponse<?> listManufacturerApplications(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ManufacturerApplication.Status appStatus = ManufacturerApplication.Status.valueOf(status);
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.ASC, "createdAt"));
        return ApiResponse.ok(PagedResponse.of(manufacturerPortalService.listPendingApplicationsByStatus(appStatus, pageable)));
    }

    /** 审批通过厂商申请 */
    @PatchMapping("/manufacturer-applications/{id}/approve")
    public ApiResponse<Void> approveManufacturerApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminUserId = userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "用户不存在"));
        manufacturerPortalService.approveApplication(id, adminUserId);
        return ApiResponse.ok();
    }

    /** 拒绝厂商申请 */
    @PatchMapping("/manufacturer-applications/{id}/reject")
    public ApiResponse<Void> rejectManufacturerApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        manufacturerPortalService.rejectApplication(id, reason);
        return ApiResponse.ok();
    }

    // ==================== 会员管理 ====================

    /** 查询所有套餐 */
    @GetMapping("/membership/plans")
    public ApiResponse<List<MembershipPlanDTO>> listMembershipPlans() {
        List<MembershipPlanDTO> plans = membershipPlanRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getSortOrder() - b.getSortOrder())
                .map(MembershipPlanDTO::from)
                .collect(Collectors.toList());
        return ApiResponse.ok(plans);
    }

    /** 创建套餐 */
    @PostMapping("/membership/plans")
    public ApiResponse<MembershipPlanDTO> createMembershipPlan(@RequestBody Map<String, Object> body) {
        MembershipPlan plan = buildPlanFromBody(new MembershipPlan(), body);
        return ApiResponse.ok(MembershipPlanDTO.from(membershipPlanRepository.save(plan)));
    }

    /** 更新套餐 */
    @PutMapping("/membership/plans/{id}")
    public ApiResponse<MembershipPlanDTO> updateMembershipPlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在: " + id));
        buildPlanFromBody(plan, body);
        return ApiResponse.ok(MembershipPlanDTO.from(membershipPlanRepository.save(plan)));
    }

    /** 查询订单列表（支持按状态筛选） */
    @GetMapping("/membership/orders")
    public ApiResponse<?> listMembershipOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentOrder> orders;
        if (status != null && !status.isBlank()) {
            PaymentOrder.Status orderStatus = PaymentOrder.Status.valueOf(status);
            orders = membershipService.listAllOrders(pageable);
            // filter by status
            List<OrderDTO> filtered = orders.getContent().stream()
                    .filter(o -> o.getStatus() == orderStatus)
                    .map(OrderDTO::from)
                    .collect(Collectors.toList());
            return ApiResponse.ok(filtered);
        }
        orders = membershipService.listAllOrders(pageable);
        return ApiResponse.ok(PagedResponse.of(orders.map(OrderDTO::from)));
    }

    /** 手动激活会员 */
    @PostMapping("/membership/orders/{orderNo}/activate")
    public ApiResponse<Void> manualActivateMembership(
            @PathVariable String orderNo,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(body.get("userId").toString());
        Long planId = Long.parseLong(body.get("planId").toString());
        int durationDays = body.containsKey("durationDays") ? Integer.parseInt(body.get("durationDays").toString()) : 30;
        membershipService.manualActivate(userId, planId, durationDays);
        return ApiResponse.ok();
    }

    private MembershipPlan buildPlanFromBody(MembershipPlan plan, Map<String, Object> body) {
        if (body.containsKey("name")) plan.setName((String) body.get("name"));
        if (body.containsKey("nameEn")) plan.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("description")) plan.setDescription((String) body.get("description"));
        if (body.containsKey("priceCny")) plan.setPriceCny(new BigDecimal(body.get("priceCny").toString()));
        if (body.containsKey("priceUsd")) plan.setPriceUsd(new BigDecimal(body.get("priceUsd").toString()));
        if (body.containsKey("durationDays")) plan.setDurationDays(Integer.parseInt(body.get("durationDays").toString()));
        if (body.containsKey("maxCompareRobots")) plan.setMaxCompareRobots(Integer.parseInt(body.get("maxCompareRobots").toString()));
        if (body.containsKey("maxExportsPerDay")) plan.setMaxExportsPerDay(Integer.parseInt(body.get("maxExportsPerDay").toString()));
        if (body.containsKey("apiAccess")) plan.setApiAccess(Boolean.parseBoolean(body.get("apiAccess").toString()));
        if (body.containsKey("isActive")) plan.setIsActive(Boolean.parseBoolean(body.get("isActive").toString()));
        if (body.containsKey("sortOrder")) plan.setSortOrder(Integer.parseInt(body.get("sortOrder").toString()));
        @SuppressWarnings("unchecked")
        List<String> features = (List<String>) body.get("features");
        if (features != null) plan.setFeatures(features);
        return plan;
    }
}
