package com.lookforbest.controller;

import com.lookforbest.dto.request.CreateOrderRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.MembershipPlanDTO;
import com.lookforbest.dto.response.OrderDTO;
import com.lookforbest.dto.response.UserMembershipDTO;
import com.lookforbest.entity.PaymentOrder;
import com.lookforbest.entity.UserMembership;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final UserRepository userRepository;

    /** 获取所有有效套餐（公开接口） */
    @GetMapping("/plans")
    public ApiResponse<List<MembershipPlanDTO>> getPlans() {
        return ApiResponse.ok(membershipService.getActivePlans());
    }

    /** 获取当前用户会员信息 */
    @GetMapping("/my")
    public ApiResponse<?> getMyMembership(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.ok(membershipService.getUserMembershipInfo(userId));
    }

    /** 创建订单 */
    @PostMapping("/orders")
    public ApiResponse<OrderDTO> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        Long userId = getUserId(userDetails);
        PaymentOrder order = membershipService.createOrder(userId, request.getPlanId(), request.getPaymentMethod());
        return ApiResponse.ok(OrderDTO.from(order));
    }

    /** 查询我的订单 */
    @GetMapping("/orders")
    public ApiResponse<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<OrderDTO> orders = membershipService.listUserOrders(userId)
                .stream()
                .map(OrderDTO::from)
                .collect(Collectors.toList());
        return ApiResponse.ok(orders);
    }

    /** 模拟支付（测试/演示用） */
    @PostMapping("/orders/{orderNo}/simulate-pay")
    public ApiResponse<OrderDTO> simulatePay(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNo) {
        PaymentOrder order = membershipService.simulatePayment(orderNo);
        return ApiResponse.ok(OrderDTO.from(order));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("用户不存在"))
                .getId();
    }
}
