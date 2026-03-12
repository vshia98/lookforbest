package com.lookforbest.controller;

import com.lookforbest.dto.request.CaseStudyCreateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.CaseStudyDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.CaseStudy;
import com.lookforbest.entity.User;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.UgcService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/case-studies")
@RequiredArgsConstructor
public class CaseStudyController {

    private final UgcService ugcService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(required = false) String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        size = Math.min(size, 100);
        Long currentUserId = resolveUserId(userDetails);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(PagedResponse.of(ugcService.getCaseStudies(industry, currentUserId, pageable)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CaseStudyDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = resolveUserId(userDetails);
        return ApiResponse.ok(ugcService.getCaseStudyById(id, currentUserId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CaseStudyDTO> create(
            @Valid @RequestBody CaseStudyCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        CaseStudy cs = ugcService.createCaseStudy(userId, request);
        return ApiResponse.ok(CaseStudyDTO.from(cs, false));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        ugcService.submitCaseStudy(userId, id);
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
        ugcService.deleteCaseStudy(userId, id, isAdmin);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Boolean>> like(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        boolean liked = ugcService.toggleLike(userId, "case_study", id);
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
        ugcService.createReport(userId, "case_study", id, reason);
        return ApiResponse.ok();
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId).orElse(null);
    }

    private Long requireUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    }
}
