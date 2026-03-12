package com.lookforbest.controller;

import com.lookforbest.dto.request.CompareRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.RobotSummaryDTO;
import com.lookforbest.entity.CompareSession;
import com.lookforbest.entity.Robot;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.service.CompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/compare")
@RequiredArgsConstructor
@Tag(name = "对比", description = "机器人对比接口")
public class CompareController {

    private final CompareService compareService;
    private final RobotRepository robotRepository;

    @PostMapping
    @Operation(summary = "创建对比会话")
    public ApiResponse<?> create(
            @Valid @RequestBody CompareRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CompareSession session = compareService.createSession(request, null);
        return ApiResponse.ok(buildCompareResult(session));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "获取对比会话")
    public ApiResponse<?> get(@PathVariable String sessionId) {
        CompareSession session = compareService.getSession(sessionId);
        return ApiResponse.ok(buildCompareResult(session));
    }

    private CompareResult buildCompareResult(CompareSession session) {
        List<Robot> robots = session.getRobotIds().stream()
                .map(id -> robotRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Robot", id)))
                .collect(Collectors.toList());

        List<String> dimensions = List.of("payloadKg", "reachMm", "dof", "repeatabilityMm", "weightKg");
        Map<String, Long> winner = computeWinner(robots, dimensions);

        return CompareResult.builder()
                .sessionId(session.getId())
                .robots(robots.stream().map(RobotSummaryDTO::from).collect(Collectors.toList()))
                .comparison(CompareResult.Comparison.builder()
                        .dimensions(dimensions)
                        .winner(winner)
                        .build())
                .build();
    }

    private Map<String, Long> computeWinner(List<Robot> robots, List<String> dimensions) {
        Map<String, Long> winners = new HashMap<>();
        for (String dim : dimensions) {
            Robot winner = null;
            BigDecimal best = null;
            for (Robot r : robots) {
                BigDecimal val = getDimValue(r, dim);
                if (val != null && (best == null || val.compareTo(best) > 0)) {
                    best = val;
                    winner = r;
                }
            }
            if (winner != null) {
                winners.put(dim, winner.getId());
            }
        }
        return winners;
    }

    private BigDecimal getDimValue(Robot r, String dim) {
        return switch (dim) {
            case "payloadKg" -> r.getPayloadKg();
            case "reachMm" -> r.getReachMm() != null ? BigDecimal.valueOf(r.getReachMm()) : null;
            case "dof" -> r.getDof() != null ? BigDecimal.valueOf(r.getDof()) : null;
            case "repeatabilityMm" -> r.getRepeatabilityMm();
            case "weightKg" -> r.getWeightKg();
            default -> null;
        };
    }

    @Getter
    @Builder
    static class CompareResult {
        private String sessionId;
        private List<RobotSummaryDTO> robots;
        private Comparison comparison;

        @Getter
        @Builder
        static class Comparison {
            private List<String> dimensions;
            private Map<String, Long> winner;
        }
    }
}
