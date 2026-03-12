package com.lookforbest.repository;

import com.lookforbest.entity.RobotComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotCommentRepository extends JpaRepository<RobotComment, Long> {

    // 顶级评论（无父评论），按时间倒序分页
    Page<RobotComment> findByRobotIdAndParentIdIsNullAndStatus(
            Long robotId, RobotComment.CommentStatus status, Pageable pageable);

    // 查某条评论的所有直接回复
    List<RobotComment> findByParentIdAndStatusOrderByCreatedAtAsc(
            Long parentId, RobotComment.CommentStatus status);

    // 统计机器人评论数
    long countByRobotIdAndStatus(Long robotId, RobotComment.CommentStatus status);

    // 查某用户在某机器人下的所有评论（用于权限校验）
    @Query("SELECT c FROM RobotComment c WHERE c.robot.id = :robotId AND c.user.id = :userId AND c.id = :commentId")
    java.util.Optional<RobotComment> findByIdAndRobotIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("robotId") Long robotId,
            @Param("userId") Long userId);
}
