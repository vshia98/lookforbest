package com.lookforbest.dto.response;

import com.lookforbest.entity.RobotComment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentDTO {

    private Long id;
    private Long robotId;
    private Long parentId;
    private String content;
    private Byte rating;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 作者信息（内嵌）
    private AuthorDTO author;

    // 子回复（仅顶级评论携带）
    private List<CommentDTO> replies;

    @Getter
    @Setter
    public static class AuthorDTO {
        private Long id;
        private String username;
        private String avatarUrl;
    }

    public static CommentDTO from(RobotComment c) {
        CommentDTO dto = new CommentDTO();
        dto.setId(c.getId());
        dto.setRobotId(c.getRobot().getId());
        dto.setParentId(c.getParentId());
        dto.setContent(c.getContent());
        dto.setRating(c.getRating());
        dto.setLikeCount(c.getLikeCount());
        dto.setStatus(c.getStatus().name());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        AuthorDTO author = new AuthorDTO();
        author.setId(c.getUser().getId());
        author.setUsername(c.getUser().getUsername());
        author.setAvatarUrl(c.getUser().getAvatarUrl());
        dto.setAuthor(author);

        return dto;
    }
}
