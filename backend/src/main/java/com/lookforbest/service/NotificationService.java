package com.lookforbest.service;

import com.lookforbest.dto.request.AnnouncementRequest;
import com.lookforbest.dto.response.NotificationDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.Notification;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.User;
import java.math.BigDecimal;
import com.lookforbest.repository.NotificationRepository;
import com.lookforbest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /** 发送通知给单个用户（内部调用） */
    @Async
    @Transactional
    public void send(Long userId, Notification.NotificationType type, String title, String content,
                     Long relatedId, String relatedType) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification n = new Notification();
            n.setUser(user);
            n.setType(type);
            n.setTitle(title);
            n.setContent(content);
            n.setRelatedId(relatedId);
            n.setRelatedType(relatedType);
            notificationRepository.save(n);
        });
    }

    /** 获取当前用户通知列表（分页） */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getMyNotifications(String userEmail, int page, int size) {
        User user = findUser(userEmail);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> pageResult = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PagedResponse.<NotificationDTO>builder()
                .content(pageResult.getContent().stream().map(NotificationDTO::from).collect(Collectors.toList()))
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .total(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .hasNext(pageResult.hasNext())
                .build();
    }

    /** 未读通知数量 */
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = findUser(userEmail);
        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    /** 标记单条已读 */
    @Transactional
    public void markRead(Long notificationId, String userEmail) {
        User user = findUser(userEmail);
        notificationRepository.markReadById(notificationId, user.getId());
    }

    /** 全部标记已读 */
    @Transactional
    public void markAllRead(String userEmail) {
        User user = findUser(userEmail);
        notificationRepository.markAllReadByUserId(user.getId());
    }

    /** 管理员：广播系统公告给全部用户 */
    @Transactional
    public void broadcastAnnouncement(AnnouncementRequest req) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            Notification n = new Notification();
            n.setUser(user);
            n.setType(Notification.NotificationType.system);
            n.setTitle(req.getTitle());
            n.setContent(req.getContent());
            notificationRepository.save(n);
        }
    }

    /** 发送降价提醒通知 */
    @Async
    public void sendPriceAlert(Long userId, Robot robot, BigDecimal currentPrice) {
        send(userId, Notification.NotificationType.price_drop,
                "降价提醒：" + robot.getName(),
                String.format("您关注的机器人「%s」当前价格为 %.2f 元，已达到您设置的降价目标。", robot.getName(), currentPrice),
                robot.getId(), "robot");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    }
}
