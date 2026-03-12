package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/更新广告请求
 */
@Data
public class AdCreateRequest {

    @NotBlank
    private String title;

    private String description;

    private String imageUrl;

    private String linkUrl;

    @NotNull
    private String position;

    private String adType = "banner";

    private Integer priority = 0;

    private Boolean isActive = true;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long manufacturerId;
}
