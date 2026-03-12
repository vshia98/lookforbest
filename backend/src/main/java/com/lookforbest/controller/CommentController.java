package com.lookforbest.controller;

import com.lookforbest.dto.request.CommentCreateRequest;
import com.lookforbest.dto.request.CommentUpdateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.CommentDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/robots/{robotId}/comments")
@RequiredArgsConstructor
@Tag(name = "评论", description = "机器人评论与回复接口")
public class CommentController {

    private final CommentService commentService;

    /** 获取评论列表（公开） */
    @GetMapping
    @Operation(summary = "获取机器人评论列表（含回复），分页")
    public ApiResponse<PagedResponse<CommentDTO>> list(
            @PathVariable Long robotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(commentService.getComments(robotId, page, size));
    }

    /** 发表评论（需登录） */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "发表评论或回复")
    public ApiResponse<CommentDTO> create(
            @PathVariable Long robotId,
            @Valid @RequestBody CommentCreateRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.ok(commentService.createComment(robotId, user.getUsername(), req));
    }

    /** 修改评论（仅作者） */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改评论内容（仅作者）")
    public ApiResponse<CommentDTO> update(
            @PathVariable Long robotId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.ok(commentService.updateComment(robotId, commentId, user.getUsername(), req));
    }

    /** 删除评论（作者或管理员） */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除评论（作者本人或管理员）")
    public ApiResponse<Void> delete(
            @PathVariable Long robotId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(robotId, commentId, user.getUsername(), isAdmin);
        return ApiResponse.ok();
    }
}
