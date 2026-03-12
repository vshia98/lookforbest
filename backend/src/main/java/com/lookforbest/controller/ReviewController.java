package com.lookforbest.controller;

import com.lookforbest.dto.request.ReviewCreateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.dto.response.ReviewDTO;
import com.lookforbest.entity.RobotReview;
import com.lookforbest.entity.User;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.UgcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final UgcService ugcService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(required = false) Long robotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        size = Math.min(size, 100);
        Long currentUserId = resolveUserId(userDetails);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (robotId != null) {
            return ApiResponse.ok(PagedResponse.of(ugcService.getReviewsByRobot(robotId, currentUserId, pageable)));
        }
        return ApiResponse.ok(PagedResponse.of(ugcService.getReviewsByStatus(RobotReview.Status.published, pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReviewDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = resolveUserId(userDetails);
        return ApiResponse.ok(ugcService.getReviewById(id, currentUserId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewDTO> create(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        RobotReview review = ugcService.createReview(userId, request);
        return ApiResponse.ok(ReviewDTO.from(review, false));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        ugcService.submitReview(userId, id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
        ugcService.deleteReview(userId, id, isAdmin);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Boolean>> like(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        boolean liked = ugcService.toggleLike(userId, "review", id);
        return ApiResponse.ok(Map.of("liked", liked));
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> report(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        String reason = body.getOrDefault("reason", "");
        ugcService.createReport(userId, "review", id, reason);
        return ApiResponse.ok();
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> myReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        size = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, size);
        return ApiResponse.ok(PagedResponse.of(ugcService.getMyReviews(userId, pageable)));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId).orElse(null);
    }

    private Long requireUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "用户不存在"));
    }
}
