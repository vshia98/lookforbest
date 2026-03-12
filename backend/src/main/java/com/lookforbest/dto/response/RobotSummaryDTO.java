package com.lookforbest.dto.response;

import com.lookforbest.entity.Robot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RobotSummaryDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String modelNumber;
    private String slug;
    private String coverImageUrl;
    private ManufacturerSummaryDTO manufacturer;
    private CategorySummaryDTO category;
    private BigDecimal payloadKg;
    private Integer reachMm;
    private Integer dof;
    private BigDecimal repeatabilityMm;
    private Boolean has3dModel;
    private String priceRange;
    private String status;
    private Long viewCount;
    private Integer favoriteCount;

    public static RobotSummaryDTO from(Robot r) {
        return RobotSummaryDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .modelNumber(r.getModelNumber())
                .slug(r.getSlug())
                .coverImageUrl(r.getCoverImageUrl())
                .manufacturer(r.getManufacturer() != null ? ManufacturerSummaryDTO.from(r.getManufacturer()) : null)
                .category(r.getCategory() != null ? CategorySummaryDTO.from(r.getCategory()) : null)
                .payloadKg(r.getPayloadKg())
                .reachMm(r.getReachMm())
                .dof(r.getDof() != null ? r.getDof().intValue() : null)
                .repeatabilityMm(r.getRepeatabilityMm())
                .has3dModel(r.getHas3dModel())
                .priceRange(r.getPriceRange() != null ? r.getPriceRange().name() : null)
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .viewCount(r.getViewCount())
                .favoriteCount(r.getFavoriteCount())
                .build();
    }

    @Getter
    @Builder
    public static class ManufacturerSummaryDTO {
        private Long id;
        private String name;
        private String logoUrl;
        private String country;

        public static ManufacturerSummaryDTO from(com.lookforbest.entity.Manufacturer m) {
            return ManufacturerSummaryDTO.builder()
                    .id(m.getId())
                    .name(m.getName())
                    .logoUrl(m.getLogoUrl())
                    .country(m.getCountry())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategorySummaryDTO {
        private Long id;
        private String name;
        private String slug;

        public static CategorySummaryDTO from(com.lookforbest.entity.RobotCategory c) {
            return CategorySummaryDTO.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .slug(c.getSlug())
                    .build();
        }
    }
}
