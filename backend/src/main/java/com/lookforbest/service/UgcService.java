package com.lookforbest.service;

import com.lookforbest.dto.request.CaseStudyCreateRequest;
import com.lookforbest.dto.request.ReviewCreateRequest;
import com.lookforbest.dto.response.CaseStudyDTO;
import com.lookforbest.dto.response.ReviewDTO;
import com.lookforbest.entity.*;
import com.lookforbest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UgcService {

    private final RobotReviewRepository reviewRepository;
    private final CaseStudyRepository caseStudyRepository;
    private final UgcLikeRepository likeRepository;
    private final UgcReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RobotRepository robotRepository;

    // ── Reviews ──────────────────────────────────────────────────────────────

    @Transactional
    public RobotReview createReview(Long userId, ReviewCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        Robot robot = robotRepository.findById(req.getRobotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "机器人不存在"));

        RobotReview review = new RobotReview();
        review.setUser(user);
        review.setRobot(robot);
        review.setTitle(req.getTitle());
        review.setContent(req.getContent());
        review.setPros(req.getPros());
        review.setCons(req.getCons());
        review.setRating(req.getRating() != null ? req.getRating().byteValue() : 5);
        review.setImages(req.getImages());
        review.setStatus(RobotReview.Status.draft);
        return reviewRepository.save(review);
    }

    @Transactional
    public void submitReview(Long userId, Long reviewId) {
        RobotReview review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评测不存在或无权操作"));
        if (review.getStatus() != RobotReview.Status.draft && review.getStatus() != RobotReview.Status.rejected) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可提交");
        }
        review.setStatus(RobotReview.Status.pending_review);
        reviewRepository.save(review);
    }

    @Transactional
    public void publishReview(Long reviewId) {
        RobotReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评测不存在"));
        review.setStatus(RobotReview.Status.published);
        review.setRejectReason(null);
        reviewRepository.save(review);
    }

    @Transactional
    public void rejectReview(Long reviewId, String reason) {
        RobotReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评测不存在"));
        review.setStatus(RobotReview.Status.rejected);
        review.setRejectReason(reason);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId, boolean isAdmin) {
        RobotReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评测不存在"));
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByRobot(Long robotId, Long currentUserId, Pageable pageable) {
        return reviewRepository.findByRobotIdAndStatus(robotId, RobotReview.Status.published, pageable)
                .map(r -> {
                    boolean liked = currentUserId != null &&
                            likeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, UgcLike.TargetType.review, r.getId());
                    return ReviewDTO.from(r, liked);
                });
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getMyReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(r -> ReviewDTO.from(r, false));
    }

    @Transactional
    public ReviewDTO getReviewById(Long reviewId, Long currentUserId) {
        RobotReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评测不存在"));
        review.setViewCount(review.getViewCount() + 1);
        reviewRepository.save(review);
        boolean liked = currentUserId != null &&
                likeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, UgcLike.TargetType.review, reviewId);
        return ReviewDTO.from(review, liked);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByStatus(RobotReview.Status status, Pageable pageable) {
        return reviewRepository.findByStatus(status, pageable)
                .map(r -> ReviewDTO.from(r, false));
    }

    // ── Case Studies ──────────────────────────────────────────────────────────

    @Transactional
    public CaseStudy createCaseStudy(Long userId, CaseStudyCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        CaseStudy cs = new CaseStudy();
        cs.setUser(user);
        cs.setTitle(req.getTitle());
        cs.setContent(req.getContent());
        cs.setRobotIds(req.getRobotIds());
        cs.setIndustry(req.getIndustry());
        cs.setImages(req.getImages());
        cs.setStatus(CaseStudy.Status.draft);
        return caseStudyRepository.save(cs);
    }

    @Transactional
    public void submitCaseStudy(Long userId, Long caseId) {
        CaseStudy cs = caseStudyRepository.findByIdAndUserId(caseId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "案例不存在或无权操作"));
        if (cs.getStatus() != CaseStudy.Status.draft && cs.getStatus() != CaseStudy.Status.rejected) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可提交");
        }
        cs.setStatus(CaseStudy.Status.pending_review);
        caseStudyRepository.save(cs);
    }

    @Transactional
    public void publishCaseStudy(Long caseId) {
        CaseStudy cs = caseStudyRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "案例不存在"));
        cs.setStatus(CaseStudy.Status.published);
        cs.setRejectReason(null);
        caseStudyRepository.save(cs);
    }

    @Transactional
    public void rejectCaseStudy(Long caseId, String reason) {
        CaseStudy cs = caseStudyRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "案例不存在"));
        cs.setStatus(CaseStudy.Status.rejected);
        cs.setRejectReason(reason);
        caseStudyRepository.save(cs);
    }

    @Transactional(readOnly = true)
    public Page<CaseStudyDTO> getCaseStudies(String industry, Long currentUserId, Pageable pageable) {
        Page<CaseStudy> page = (industry != null && !industry.isBlank())
                ? caseStudyRepository.findByIndustryAndStatus(industry, CaseStudy.Status.published, pageable)
                : caseStudyRepository.findByStatus(CaseStudy.Status.published, pageable);
        return page.map(cs -> {
            boolean liked = currentUserId != null &&
                    likeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, UgcLike.TargetType.case_study, cs.getId());
            return CaseStudyDTO.from(cs, liked);
        });
    }

    @Transactional
    public CaseStudyDTO getCaseStudyById(Long id, Long currentUserId) {
        CaseStudy cs = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "案例不存在"));
        cs.setViewCount(cs.getViewCount() + 1);
        caseStudyRepository.save(cs);
        boolean liked = currentUserId != null &&
                likeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, UgcLike.TargetType.case_study, id);
        return CaseStudyDTO.from(cs, liked);
    }

    @Transactional
    public void deleteCaseStudy(Long userId, Long caseId, boolean isAdmin) {
        CaseStudy cs = caseStudyRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "案例不存在"));
        if (!isAdmin && !cs.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除");
        }
        caseStudyRepository.delete(cs);
    }

    @Transactional(readOnly = true)
    public Page<CaseStudyDTO> getCaseStudiesByStatus(CaseStudy.Status status, Pageable pageable) {
        return caseStudyRepository.findByStatus(status, pageable)
                .map(cs -> CaseStudyDTO.from(cs, false));
    }

    // ── Likes & Reports ───────────────────────────────────────────────────────

    @Transactional
    public boolean toggleLike(Long userId, String targetTypeStr, Long targetId) {
        UgcLike.TargetType targetType = UgcLike.TargetType.valueOf(targetTypeStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        return likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(like -> {
                    likeRepository.delete(like);
                    decrementLikeCount(targetType, targetId);
                    return false;
                })
                .orElseGet(() -> {
                    UgcLike like = new UgcLike();
                    like.setUser(user);
                    like.setTargetType(targetType);
                    like.setTargetId(targetId);
                    likeRepository.save(like);
                    incrementLikeCount(targetType, targetId);
                    return true;
                });
    }

    private void incrementLikeCount(UgcLike.TargetType type, Long targetId) {
        if (type == UgcLike.TargetType.review) {
            reviewRepository.findById(targetId).ifPresent(r -> {
                r.setLikeCount(r.getLikeCount() + 1);
                reviewRepository.save(r);
            });
        } else {
            caseStudyRepository.findById(targetId).ifPresent(cs -> {
                cs.setLikeCount(cs.getLikeCount() + 1);
                caseStudyRepository.save(cs);
            });
        }
    }

    private void decrementLikeCount(UgcLike.TargetType type, Long targetId) {
        if (type == UgcLike.TargetType.review) {
            reviewRepository.findById(targetId).ifPresent(r -> {
                r.setLikeCount(Math.max(0, r.getLikeCount() - 1));
                reviewRepository.save(r);
            });
        } else {
            caseStudyRepository.findById(targetId).ifPresent(cs -> {
                cs.setLikeCount(Math.max(0, cs.getLikeCount() - 1));
                caseStudyRepository.save(cs);
            });
        }
    }

    @Transactional
    public void createReport(Long userId, String targetTypeStr, Long targetId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        UgcReport report = new UgcReport();
        report.setUser(user);
        report.setTargetType(UgcReport.TargetType.valueOf(targetTypeStr));
        report.setTargetId(targetId);
        report.setReason(reason);
        reportRepository.save(report);
    }
}
