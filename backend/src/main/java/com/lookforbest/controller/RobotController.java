package com.lookforbest.controller;

import com.lookforbest.dto.request.RobotCreateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.dto.response.RobotDetailDTO;
import com.lookforbest.dto.response.RobotSummaryDTO;
import com.lookforbest.entity.Robot;
import com.lookforbest.repository.RobotDocumentRepository;
import com.lookforbest.repository.RobotImageRepository;
import com.lookforbest.repository.RobotVideoRepository;
import com.lookforbest.service.FavoriteService;
import com.lookforbest.service.impl.RobotServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/robots")
@RequiredArgsConstructor
@Tag(name = "机器人", description = "机器人信息相关接口")
public class RobotController {

    private final RobotServiceImpl robotService;
    private final FavoriteService favoriteService;
    private final RobotImageRepository imageRepository;
    private final RobotVideoRepository videoRepository;
    private final RobotDocumentRepository documentRepository;
    private final com.lookforbest.service.RecommendService recommendService;

    @GetMapping
    @Operation(summary = "获取机器人列表", description = "支持多维度筛选和搜索")
    public ApiResponse<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false) List<Long> domainIds,
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
        Page<Robot> robots = robotService.findAll(
                q, categoryId, manufacturerId, domainIds,
                payloadMin, payloadMax, reachMin, reachMax,
                dofMin, dofMax, has3dModel, status, sort,
                PageRequest.of(page, size)
        );
        return ApiResponse.ok(PagedResponse.of(robots.map(RobotSummaryDTO::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取机器人详情")
    public ApiResponse<?> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Robot robot = robotService.findById(id);
        String email = userDetails != null ? userDetails.getUsername() : null;
        boolean favorited = favoriteService.isFavorited(email, id);
        return ApiResponse.ok(RobotDetailDTO.from(
                robot,
                imageRepository.findByRobotIdOrderBySortOrderAsc(id),
                videoRepository.findByRobotIdOrderBySortOrderAsc(id),
                documentRepository.findByRobotIdOrderByCreatedAtAsc(id),
                favorited,
                robotService.findSimilar(id, 6)
        ));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "通过slug获取机器人详情")
    public ApiResponse<?> getBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails) {
        Robot robot = robotService.findBySlug(slug);
        String email = userDetails != null ? userDetails.getUsername() : null;
        boolean favorited = favoriteService.isFavorited(email, robot.getId());
        return ApiResponse.ok(RobotDetailDTO.from(
                robot,
                imageRepository.findByRobotIdOrderBySortOrderAsc(robot.getId()),
                videoRepository.findByRobotIdOrderBySortOrderAsc(robot.getId()),
                documentRepository.findByRobotIdOrderByCreatedAtAsc(robot.getId()),
                favorited,
                robotService.findSimilar(robot.getId(), 6)
        ));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "获取相似机器人（分类降级）")
    public ApiResponse<?> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int size) {
        return ApiResponse.ok(
                robotService.findSimilar(id, size)
                        .stream()
                        .map(RobotSummaryDTO::from)
                        .toList()
        );
    }

    @GetMapping("/{id}/recommend")
    @Operation(summary = "获取智能推荐机器人（AI推荐服务）")
    public ApiResponse<?> recommend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "hybrid") String strategy) {
        return ApiResponse.ok(recommendService.getSimilar(id, size, strategy));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "创建机器人（管理员）")
    public ApiResponse<?> create(@Valid @RequestBody RobotCreateRequest request) {
        Robot robot = robotService.buildFromRequest(request);
        return ApiResponse.ok(RobotSummaryDTO.from(robotService.create(robot)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "更新机器人（管理员）")
    public ApiResponse<?> update(
            @PathVariable Long id,
            @Valid @RequestBody RobotCreateRequest request) {
        Robot updated = robotService.buildFromRequest(request);
        return ApiResponse.ok(RobotSummaryDTO.from(robotService.update(id, updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "删除机器人（管理员）")
    public ApiResponse<?> delete(@PathVariable Long id) {
        robotService.delete(id);
        return ApiResponse.ok();
    }
}
