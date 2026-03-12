package com.lookforbest.service;

import com.lookforbest.dto.request.ManufacturerApplicationRequest;
import com.lookforbest.dto.response.ManufacturerPortalDTO;
import com.lookforbest.entity.*;
import com.lookforbest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManufacturerPortalService {

    private final ManufacturerApplicationRepository applicationRepository;
    private final ManufacturerUserRepository manufacturerUserRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final UserRepository userRepository;
    private final RobotRepository robotRepository;
    private final InquiryRepository inquiryRepository;

    /** 提交入驻申请 */
    @Transactional
    public ManufacturerApplication applyForPortal(Long userId, ManufacturerApplicationRequest req) {
        // 检查是否已有待审核或已通过的申请
        Optional<ManufacturerApplication> existing = applicationRepository.findByUserIdAndStatus(userId, ManufacturerApplication.Status.pending);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已有一个待审核的申请");
        }
        Optional<ManufacturerApplication> approved = applicationRepository.findByUserIdAndStatus(userId, ManufacturerApplication.Status.approved);
        if (approved.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您的申请已通过，无需重复申请");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        ManufacturerApplication application = new ManufacturerApplication();
        application.setUser(user);
        application.setManufacturerId(req.getManufacturerId());
        application.setCompanyName(req.getCompanyName());
        application.setCompanyNameEn(req.getCompanyNameEn());
        application.setCountry(req.getCountry());
        application.setWebsiteUrl(req.getWebsiteUrl());
        application.setContactPerson(req.getContactPerson());
        application.setContactEmail(req.getContactEmail());
        application.setContactPhone(req.getContactPhone());
        application.setBusinessLicense(req.getBusinessLicense());
        application.setDescription(req.getDescription());
        application.setStatus(ManufacturerApplication.Status.pending);
        return applicationRepository.save(application);
    }

    /** 查询本人申请记录（返回最近一条） */
    @Transactional(readOnly = true)
    public Optional<ManufacturerApplication> getMyApplication(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().findFirst();
    }

    /** 审批通过：创建或关联厂商，赋予用户 manufacturer 角色 */
    @Transactional
    public void approveApplication(Long applicationId, Long adminUserId) {
        ManufacturerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (app.getStatus() != ManufacturerApplication.Status.pending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
        }

        Manufacturer manufacturer;
        if (app.getManufacturerId() != null) {
            // 认领已有厂商
            manufacturer = manufacturerRepository.findById(app.getManufacturerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "厂商不存在"));
        } else {
            // 新建厂商
            manufacturer = new Manufacturer();
            manufacturer.setName(app.getCompanyName());
            manufacturer.setNameEn(app.getCompanyNameEn());
            manufacturer.setCountry(app.getCountry());
            manufacturer.setWebsiteUrl(app.getWebsiteUrl());
            manufacturer.setDescription(app.getDescription());
            manufacturer.setContactPerson(app.getContactPerson());
            manufacturer.setContactEmail(app.getContactEmail());
            manufacturer.setContactPhone(app.getContactPhone());
            manufacturer.setBusinessLicense(app.getBusinessLicense());
            manufacturer.setIsVerified(true);
            manufacturer.setVerifiedAt(LocalDateTime.now());
            manufacturer = manufacturerRepository.save(manufacturer);
        }

        // 创建厂商-用户关联
        ManufacturerUser mu = new ManufacturerUser();
        mu.setManufacturerId(manufacturer.getId());
        mu.setUserId(app.getUser().getId());
        mu.setRole(ManufacturerUser.Role.owner);
        manufacturerUserRepository.save(mu);

        // 升级用户角色为 manufacturer
        User user = app.getUser();
        user.setRole(User.Role.manufacturer);
        userRepository.save(user);

        // 更新申请状态
        app.setStatus(ManufacturerApplication.Status.approved);
        app.setApprovedAt(LocalDateTime.now());
        app.setApprovedByUserId(adminUserId);
        applicationRepository.save(app);
    }

    /** 拒绝申请 */
    @Transactional
    public void rejectApplication(Long applicationId, String reason) {
        ManufacturerApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (app.getStatus() != ManufacturerApplication.Status.pending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
        }
        app.setStatus(ManufacturerApplication.Status.rejected);
        app.setRejectReason(reason);
        applicationRepository.save(app);
    }

    /** 获取厂商门户信息（含统计数据） */
    @Transactional(readOnly = true)
    public Optional<ManufacturerPortalDTO> getMyManufacturer(Long userId) {
        return manufacturerUserRepository.findByUserId(userId)
                .flatMap(mu -> manufacturerRepository.findById(mu.getManufacturerId()))
                .map(m -> {
                    long robotCount = robotRepository.countByManufacturerId(m.getId());
                    long inquiryCount = inquiryRepository.countByManufacturerId(m.getId());
                    long viewCount = robotRepository.sumViewCountByManufacturerId(m.getId());
                    return ManufacturerPortalDTO.from(m, robotCount, inquiryCount, viewCount);
                });
    }

    /** 更新厂商资料（仅 owner/admin 可操作） */
    @Transactional
    public void updateManufacturerProfile(Long userId, Map<String, Object> updates) {
        ManufacturerUser mu = manufacturerUserRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "您没有厂商管理权限"));
        if (mu.getRole() == ManufacturerUser.Role.member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
        }

        Manufacturer m = manufacturerRepository.findById(mu.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "厂商不存在"));

        if (updates.containsKey("websiteUrl")) m.setWebsiteUrl((String) updates.get("websiteUrl"));
        if (updates.containsKey("description")) m.setDescription((String) updates.get("description"));
        if (updates.containsKey("contactPerson")) m.setContactPerson((String) updates.get("contactPerson"));
        if (updates.containsKey("contactEmail")) m.setContactEmail((String) updates.get("contactEmail"));
        if (updates.containsKey("contactPhone")) m.setContactPhone((String) updates.get("contactPhone"));
        if (updates.containsKey("companySize")) m.setCompanySize((String) updates.get("companySize"));
        if (updates.containsKey("logoUrl")) m.setLogoUrl((String) updates.get("logoUrl"));
        if (updates.containsKey("headquarters")) m.setHeadquarters((String) updates.get("headquarters"));

        manufacturerRepository.save(m);
    }

    /** 获取厂商统计数据 */
    @Transactional(readOnly = true)
    public Map<String, Object> getPortalStats(Long manufacturerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("robotCount", robotRepository.countByManufacturerId(manufacturerId));
        stats.put("inquiryCount", inquiryRepository.countByManufacturerId(manufacturerId));
        long viewCount = robotRepository.sumViewCountByManufacturerId(manufacturerId);
        stats.put("viewCount", viewCount);
        return stats;
    }

    /** 分页获取申请列表（管理员用，支持按状态筛选） */
    @Transactional(readOnly = true)
    public Page<ManufacturerApplication> listPendingApplications(Pageable pageable) {
        return applicationRepository.findByStatus(ManufacturerApplication.Status.pending, pageable);
    }

    /** 分页获取申请列表（管理员用，指定状态） */
    @Transactional(readOnly = true)
    public Page<ManufacturerApplication> listPendingApplicationsByStatus(ManufacturerApplication.Status status, Pageable pageable) {
        return applicationRepository.findByStatus(status, pageable);
    }
}
