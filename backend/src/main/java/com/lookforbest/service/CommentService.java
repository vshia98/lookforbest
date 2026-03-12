package com.lookforbest.service;

import com.lookforbest.dto.request.CommentCreateRequest;
import com.lookforbest.dto.request.CommentUpdateRequest;
import com.lookforbest.dto.response.CommentDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotComment;
import com.lookforbest.entity.User;
import com.lookforbest.repository.RobotCommentRepository;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final RobotCommentRepository commentRepository;
    private final RobotRepository robotRepository;
    private final UserRepository userRepository;

    /** 获取机器人顶级评论（带子回复），分页 */
    @Transactional(readOnly = true)
    public PagedResponse<CommentDTO> getComments(Long robotId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RobotComment> pageResult = commentRepository.findByRobotIdAndParentIdIsNullAndStatus(
                robotId, RobotComment.CommentStatus.approved, pageable);

        List<CommentDTO> dtos = pageResult.getContent().stream()
                .map(c -> {
                    CommentDTO dto = CommentDTO.from(c);
                    List<CommentDTO> replies = commentRepository
                            .findByParentIdAndStatusOrderByCreatedAtAsc(c.getId(), RobotComment.CommentStatus.approved)
                            .stream()
                            .map(CommentDTO::from)
                            .collect(Collectors.toList());
                    dto.setReplies(replies);
                    return dto;
                })
                .collect(Collectors.toList());

        return PagedResponse.<CommentDTO>builder()
                .content(dtos)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .total(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .build();
    }

    /** 新增评论或回复 */
    @Transactional
    public CommentDTO createComment(Long robotId, String userEmail, CommentCreateRequest req) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "机器人不存在"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));

        // 校验父评论是否属于该机器人
        if (req.getParentId() != null) {
            commentRepository.findById(req.getParentId()).ifPresent(parent -> {
                if (!parent.getRobot().getId().equals(robotId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父评论不属于该机器人");
                }
            });
        }

        RobotComment comment = new RobotComment();
        comment.setRobot(robot);
        comment.setUser(user);
        comment.setContent(req.getContent());
        comment.setParentId(req.getParentId());
        // 仅顶级评论允许评分
        if (req.getParentId() == null) {
            comment.setRating(req.getRating());
        }
        comment.setStatus(RobotComment.CommentStatus.approved);

        return CommentDTO.from(commentRepository.save(comment));
    }

    /** 修改评论（仅作者可改） */
    @Transactional
    public CommentDTO updateComment(Long robotId, Long commentId, String userEmail, CommentUpdateRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));

        RobotComment comment = commentRepository.findByIdAndRobotIdAndUserId(commentId, robotId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权修改该评论"));

        comment.setContent(req.getContent());
        return CommentDTO.from(commentRepository.save(comment));
    }

    /** 删除评论（作者或管理员） */
    @Transactional
    public void deleteComment(Long robotId, Long commentId, String userEmail, boolean isAdmin) {
        if (isAdmin) {
            RobotComment comment = commentRepository.findById(commentId)
                    .filter(c -> c.getRobot().getId().equals(robotId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在"));
            commentRepository.delete(comment);
        } else {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
            RobotComment comment = commentRepository.findByIdAndRobotIdAndUserId(commentId, robotId, user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除该评论"));
            commentRepository.delete(comment);
        }
    }
}
