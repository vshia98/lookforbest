package com.lookforbest.dto.response;

import com.lookforbest.entity.MembershipPlan;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MembershipPlanDTO {

    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private BigDecimal priceCny;
    private BigDecimal priceUsd;
    private Integer durationDays;
    private List<String> features;
    private Integer maxCompareRobots;
    private Integer maxExportsPerDay;
    private Boolean apiAccess;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public static MembershipPlanDTO from(MembershipPlan plan) {
        MembershipPlanDTO dto = new MembershipPlanDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setNameEn(plan.getNameEn());
        dto.setDescription(plan.getDescription());
        dto.setPriceCny(plan.getPriceCny());
        dto.setPriceUsd(plan.getPriceUsd());
        dto.setDurationDays(plan.getDurationDays());
        dto.setFeatures(plan.getFeatures());
        dto.setMaxCompareRobots(plan.getMaxCompareRobots());
        dto.setMaxExportsPerDay(plan.getMaxExportsPerDay());
        dto.setApiAccess(plan.getApiAccess());
        dto.setIsActive(plan.getIsActive());
        dto.setSortOrder(plan.getSortOrder());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }
}
