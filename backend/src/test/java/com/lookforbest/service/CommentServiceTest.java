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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 单元测试")
class CommentServiceTest {

    @Mock
    private RobotCommentRepository commentRepository;
    @Mock
    private RobotRepository robotRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Robot testRobot;
    private User testUser;
    private RobotComment testComment;

    @BeforeEach
    void setUp() {
        testRobot = new Robot();
        testRobot.setId(1L);
        testRobot.setName("IRB 120");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setUsername("testuser");

        testComment = new RobotComment();
        testComment.setId(1L);
        testComment.setRobot(testRobot);
        testComment.setUser(testUser);
        testComment.setContent("很好的机器人");
        testComment.setStatus(RobotComment.CommentStatus.approved);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getComments - 分页获取顶级评论")
    void getComments_returnsPagedComments() {
        Page<RobotComment> page = new PageImpl<>(List.of(testComment));
        when(commentRepository.findByRobotIdAndParentIdIsNullAndStatus(
                eq(1L), eq(RobotComment.CommentStatus.approved), any(PageRequest.class)))
                .thenReturn(page);
        when(commentRepository.findByParentIdAndStatusOrderByCreatedAtAsc(
                eq(1L), eq(RobotComment.CommentStatus.approved)))
                .thenReturn(List.of());

        PagedResponse<CommentDTO> result = commentService.getComments(1L, 0, 10);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("很好的机器人");
    }

    @Test
    @DisplayName("createComment - 创建顶级评论成功")
    void createComment_topLevel_success() {
        CommentCreateRequest req = new CommentCreateRequest();
        req.setContent("很好的机器人");
        req.setRating((byte) 5);

        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(RobotComment.class))).thenAnswer(inv -> {
            RobotComment c = inv.getArgument(0);
            c.setId(10L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        CommentDTO result = commentService.createComment(1L, "user@test.com", req);

        assertThat(result.getContent()).isEqualTo("很好的机器人");
        assertThat(result.getRating()).isEqualTo((byte) 5);
    }

    @Test
    @DisplayName("createComment - 机器人不存在时抛出异常")
    void createComment_robotNotFound_throwsException() {
        when(robotRepository.findById(99L)).thenReturn(Optional.empty());

        CommentCreateRequest req = new CommentCreateRequest();
        req.setContent("测试");

        assertThatThrownBy(() -> commentService.createComment(99L, "user@test.com", req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("createComment - 用户不存在时抛出异常")
    void createComment_userNotFound_throwsException() {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(userRepository.findByEmail("nouser@test.com")).thenReturn(Optional.empty());

        CommentCreateRequest req = new CommentCreateRequest();
        req.setContent("测试");

        assertThatThrownBy(() -> commentService.createComment(1L, "nouser@test.com", req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("updateComment - 作者可以修改评论")
    void updateComment_byAuthor_success() {
        CommentUpdateRequest req = new CommentUpdateRequest();
        req.setContent("修改后的内容");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findByIdAndRobotIdAndUserId(1L, 1L, 1L))
                .thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(RobotComment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentDTO result = commentService.updateComment(1L, 1L, "user@test.com", req);

        assertThat(result.getContent()).isEqualTo("修改后的内容");
    }

    @Test
    @DisplayName("updateComment - 非作者无权修改时抛出 403")
    void updateComment_notOwner_throwsForbidden() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findByIdAndRobotIdAndUserId(1L, 1L, 1L))
                .thenReturn(Optional.empty());

        CommentUpdateRequest req = new CommentUpdateRequest();
        req.setContent("试图修改");

        assertThatThrownBy(() -> commentService.updateComment(1L, 1L, "user@test.com", req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("deleteComment - 管理员可删除任意评论")
    void deleteComment_byAdmin_success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThatCode(() -> commentService.deleteComment(1L, 1L, "admin@test.com", true))
                .doesNotThrowAnyException();
        verify(commentRepository).delete(testComment);
    }

    @Test
    @DisplayName("deleteComment - 作者可删除自己的评论")
    void deleteComment_byAuthor_success() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findByIdAndRobotIdAndUserId(1L, 1L, 1L))
                .thenReturn(Optional.of(testComment));

        assertThatCode(() -> commentService.deleteComment(1L, 1L, "user@test.com", false))
                .doesNotThrowAnyException();
        verify(commentRepository).delete(testComment);
    }

    @Test
    @DisplayName("deleteComment - 管理员删除不存在评论时抛出异常")
    void deleteComment_adminNotFound_throwsException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 99L, "admin@test.com", true))
                .isInstanceOf(ResponseStatusException.class);
    }
}
