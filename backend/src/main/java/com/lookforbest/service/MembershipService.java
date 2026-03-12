package com.lookforbest.service;

import com.lookforbest.dto.response.MembershipPlanDTO;
import com.lookforbest.dto.response.UserMembershipDTO;
import com.lookforbest.entity.MembershipPlan;
import com.lookforbest.entity.PaymentOrder;
import com.lookforbest.entity.UserMembership;
import com.lookforbest.repository.MembershipPlanRepository;
import com.lookforbest.repository.PaymentOrderRepository;
import com.lookforbest.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipPlanRepository planRepository;
    private final UserMembershipRepository membershipRepository;
    private final PaymentOrderRepository orderRepository;

    /** 获取所有有效套餐 */
    public List<MembershipPlanDTO> getActivePlans() {
        return planRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(MembershipPlanDTO::from)
                .collect(Collectors.toList());
    }

    /** 获取用户当前有效会员 */
    public Optional<UserMembership> getCurrentMembership(Long userId) {
        return membershipRepository.findByUserIdAndStatus(userId, UserMembership.Status.active);
    }

    /** 创建订单 */
    @Transactional
    public PaymentOrder createOrder(Long userId, Long planId, String paymentMethod) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在: " + planId));

        PaymentOrder.PaymentMethod method;
        try {
            method = PaymentOrder.PaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException e) {
            method = PaymentOrder.PaymentMethod.manual;
        }

        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlan(plan);
        order.setAmountCny(plan.getPriceCny());
        order.setPaymentMethod(method);

        // 免费套餐直接激活
        if (plan.getPriceCny().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(PaymentOrder.Status.paid);
            order.setPaidAt(LocalDateTime.now());
            order = orderRepository.save(order);
            activateMembership(userId, planId);
        } else {
            order.setStatus(PaymentOrder.Status.pending);
            order = orderRepository.save(order);
        }

        return order;
    }

    /** 模拟支付成功（管理/测试用） */
    @Transactional
    public PaymentOrder simulatePayment(String orderNo) {
        PaymentOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderNo));

        if (order.getStatus() != PaymentOrder.Status.pending) {
            throw new IllegalStateException("订单状态不允许支付: " + order.getStatus());
        }

        order.setStatus(PaymentOrder.Status.paid);
        order.setPaidAt(LocalDateTime.now());
        order = orderRepository.save(order);

        activateMembership(order.getUserId(), order.getPlan().getId());
        return order;
    }

    /** 激活/续期会员 */
    @Transactional
    public UserMembership activateMembership(Long userId, Long planId) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在: " + planId));

        // 检查是否已有有效会员
        Optional<UserMembership> existingOpt = membershipRepository.findByUserIdAndStatus(userId, UserMembership.Status.active);

        UserMembership membership;
        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            membership = existingOpt.get();
            // 续期：从现有到期时间或当前时间开始延长
            LocalDateTime base = (membership.getEndDate() != null && membership.getEndDate().isAfter(now))
                    ? membership.getEndDate() : now;
            membership.setPlan(plan);
            if (plan.getDurationDays() == 0) {
                membership.setEndDate(null); // 永久
            } else {
                membership.setEndDate(base.plusDays(plan.getDurationDays()));
            }
        } else {
            membership = new UserMembership();
            membership.setUserId(userId);
            membership.setPlan(plan);
            membership.setStartDate(now);
            membership.setStatus(UserMembership.Status.active);
            if (plan.getDurationDays() == 0) {
                membership.setEndDate(null);
            } else {
                membership.setEndDate(now.plusDays(plan.getDurationDays()));
            }
        }

        return membershipRepository.save(membership);
    }

    /** 检查用户是否有某项功能权限 */
    public boolean checkMembershipFeature(Long userId, String feature) {
        Optional<UserMembership> membershipOpt = getCurrentMembership(userId);
        if (membershipOpt.isEmpty()) return false;
        UserMembership membership = membershipOpt.get();
        List<String> features = membership.getPlan().getFeatures();
        return features != null && features.contains(feature);
    }

    /** 获取用户会员信息（含剩余天数） */
    public Map<String, Object> getUserMembershipInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        Optional<UserMembership> membershipOpt = getCurrentMembership(userId);

        if (membershipOpt.isPresent()) {
            UserMembership membership = membershipOpt.get();
            result.put("hasMembership", true);
            result.put("membership", UserMembershipDTO.from(membership));
        } else {
            result.put("hasMembership", false);
            result.put("membership", null);
        }
        return result;
    }

    /** 管理员：查询所有订单（分页） */
    public Page<PaymentOrder> listAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /** 查询用户自己的订单 */
    public List<PaymentOrder> listUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** 管理员手动激活会员 */
    @Transactional
    public void manualActivate(Long userId, Long planId, int durationDays) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("套餐不存在: " + planId));

        Optional<UserMembership> existingOpt = membershipRepository.findByUserIdAndStatus(userId, UserMembership.Status.active);
        UserMembership membership = existingOpt.orElseGet(UserMembership::new);

        LocalDateTime now = LocalDateTime.now();
        if (membership.getId() == null) {
            membership.setUserId(userId);
            membership.setStartDate(now);
            membership.setStatus(UserMembership.Status.active);
        }
        membership.setPlan(plan);
        if (durationDays == 0) {
            membership.setEndDate(null);
        } else {
            LocalDateTime base = (membership.getEndDate() != null && membership.getEndDate().isAfter(now))
                    ? membership.getEndDate() : now;
            membership.setEndDate(base.plusDays(durationDays));
        }
        membershipRepository.save(membership);

        // 创建手动激活订单记录
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlan(plan);
        order.setAmountCny(BigDecimal.ZERO);
        order.setPaymentMethod(PaymentOrder.PaymentMethod.manual);
        order.setStatus(PaymentOrder.Status.paid);
        order.setPaidAt(now);
        order.setRemark("管理员手动激活，有效天数: " + durationDays);
        orderRepository.save(order);
    }

    /** 生成订单号 */
    private String generateOrderNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new Random().nextInt(10000));
        return "LFB" + timestamp + random;
    }
}
