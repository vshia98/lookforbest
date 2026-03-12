package com.lookforbest.controller;

import com.lookforbest.entity.PriceAlert;
import com.lookforbest.entity.User;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.PriceTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "价格追踪", description = "机器人价格历史与降价提醒")
public class PriceTrackingController {

    private final PriceTrackingService priceTrackingService;
    private final UserRepository userRepository;

    @GetMapping("/robots/{id}/price-history")
    @Operation(summary = "获取机器人价格历史（近90天）")
    public ResponseEntity<List<Map<String, Object>>> getPriceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(priceTrackingService.getPriceHistory(id));
    }

    @PostMapping("/robots/{id}/price-alert")
    @Operation(summary = "设置降价提醒")
    public ResponseEntity<Map<String, Object>> setAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        BigDecimal targetPrice = new BigDecimal(body.get("targetPrice").toString());
        PriceAlert alert = priceTrackingService.setAlert(userId, id, targetPrice);
        return ResponseEntity.ok(Map.of("message", "降价提醒已设置", "alertId", alert.getId()));
    }

    @DeleteMapping("/robots/{id}/price-alert")
    @Operation(summary = "取消降价提醒")
    public ResponseEntity<Map<String, String>> cancelAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        priceTrackingService.cancelAlert(userId, id);
        return ResponseEntity.ok(Map.of("message", "降价提醒已取消"));
    }

    @GetMapping("/user/price-alerts")
    @Operation(summary = "获取我的降价提醒列表")
    public ResponseEntity<List<PriceAlert>> getMyAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(priceTrackingService.getUserAlerts(userId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        return user.getId();
    }
}
