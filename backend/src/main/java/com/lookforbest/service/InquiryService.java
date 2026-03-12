package com.lookforbest.service;

import com.lookforbest.dto.request.InquiryCreateRequest;
import com.lookforbest.dto.request.InquiryReplyRequest;
import com.lookforbest.dto.response.InquiryDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.Inquiry;
import com.lookforbest.entity.Manufacturer;
import com.lookforbest.entity.Notification;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.User;
import com.lookforbest.repository.InquiryRepository;
import com.lookforbest.repository.ManufacturerRepository;
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

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final RobotRepository robotRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** 用户提交询价 */
    @Transactional
    public InquiryDTO createInquiry(String userEmail, InquiryCreateRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        Robot robot = robotRepository.findById(req.getRobotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "机器人不存在"));
        Manufacturer manufacturer = robot.getManufacturer();
        if (manufacturer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该机器人没有关联厂商");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setUser(user);
        inquiry.setRobot(robot);
        inquiry.setManufacturer(manufacturer);
        inquiry.setMessage(req.getMessage());
        inquiry.setContactName(req.getContactName());
        inquiry.setContactEmail(req.getContactEmail());
        inquiry.setContactPhone(req.getContactPhone());
        inquiry.setContactCompany(req.getContactCompany());
        inquiry.setStatus(Inquiry.InquiryStatus.pending);

        return InquiryDTO.from(inquiryRepository.save(inquiry));
    }

    /** 获取当前用户的询价列表 */
    @Transactional(readOnly = true)
    public PagedResponse<InquiryDTO> getMyInquiries(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Inquiry> pageResult = inquiryRepository.findByUserId(user.getId(), pageable);
        return toPagedResponse(pageResult);
    }

    /** 获取单个询价详情（仅询价人或管理员） */
    @Transactional(readOnly = true)
    public InquiryDTO getInquiry(Long inquiryId, String userEmail, boolean isAdmin) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "询价不存在"));
        if (!isAdmin && !inquiry.getUser().getEmail().equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该询价");
        }
        return InquiryDTO.from(inquiry);
    }

    // ---- 管理员接口 ----

    /** 管理员获取所有询价列表 */
    @Transactional(readOnly = true)
    public PagedResponse<InquiryDTO> adminListAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPagedResponse(inquiryRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    /** 管理员回复询价 */
    @Transactional
    public InquiryDTO adminReply(Long inquiryId, InquiryReplyRequest req) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "询价不存在"));
        inquiry.setReplyContent(req.getReplyContent());
        inquiry.setRepliedAt(LocalDateTime.now());
        inquiry.setStatus(Inquiry.InquiryStatus.replied);
        InquiryDTO result = InquiryDTO.from(inquiryRepository.save(inquiry));
        // 通知询价用户
        notificationService.send(
                inquiry.getUser().getId(),
                Notification.NotificationType.inquiry_replied,
                "您的询价收到了回复",
                inquiry.getRobot().getName() + " - " + req.getReplyContent().substring(0, Math.min(50, req.getReplyContent().length())),
                inquiry.getId(), "inquiry"
        );
        return result;
    }

    /** 管理员更新询价状态 */
    @Transactional
    public InquiryDTO adminUpdateStatus(Long inquiryId, String status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "询价不存在"));
        try {
            inquiry.setStatus(Inquiry.InquiryStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的状态值");
        }
        return InquiryDTO.from(inquiryRepository.save(inquiry));
    }

    private PagedResponse<InquiryDTO> toPagedResponse(Page<Inquiry> page) {
        return PagedResponse.<InquiryDTO>builder()
                .content(page.getContent().stream().map(InquiryDTO::from).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }
}
