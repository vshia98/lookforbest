package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RobotCreateRequest {

    @NotNull
    private Long manufacturerId;

    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    private String nameEn;
    private String subtitle;
    private String subtitleEn;
    private String modelNumber;
    private String description;
    private String descriptionEn;
    private String introduction;
    private String introductionEn;
    private String applicationScenarios;
    private String applicationScenariosEn;
    private String advantages;
    private String advantagesEn;
    private Short releaseYear;
    private String status;

    private Byte dof;
    private BigDecimal payloadKg;
    private Integer reachMm;
    private BigDecimal repeatabilityMm;
    private BigDecimal maxSpeedDegS;
    private BigDecimal weightKg;
    private String ipRating;
    private String mounting;
    private BigDecimal maxSpeedMs;
    private BigDecimal maxLoadKg;
    private BigDecimal batteryLifeH;
    private String navigationType;
    private Integer heightMm;
    private BigDecimal walkingSpeedMs;

    private Map<String, Object> extraSpecs;
    private String priceRange;
    private BigDecimal priceUsdFrom;
    private String coverImageUrl;
    private List<Map<String, String>> contentImages;
    private List<String> videoUrls;
    /** 标签名称列表，不存在的会自动创建 */
    private List<String> tagNames;
    private String isOpenSource;
    private LocalDate listedDate;
    private List<Long> applicationDomainIds;
}
