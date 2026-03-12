package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.DomainDTO;
import com.lookforbest.repository.ApplicationDomainRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/domains")
@RequiredArgsConstructor
@Tag(name = "应用领域", description = "应用领域列表接口")
public class DomainController {

    private final ApplicationDomainRepository domainRepository;

    @GetMapping
    @Operation(summary = "获取应用领域列表")
    public ApiResponse<?> list() {
        return ApiResponse.ok(
                domainRepository.findAllByOrderByNameAsc()
                        .stream()
                        .map(DomainDTO::from)
                        .collect(Collectors.toList())
        );
    }
}
