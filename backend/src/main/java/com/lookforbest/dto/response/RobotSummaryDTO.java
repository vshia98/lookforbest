package com.lookforbest.dto.response;

import com.lookforbest.entity.Robot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RobotSummaryDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String subtitle;
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
    private List<String> tags;
    private Long viewCount;
    private Integer favoriteCount;

    public static RobotSummaryDTO from(Robot r) {
        return RobotSummaryDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .subtitle(r.getSubtitle())
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
                .priceRange(r.getPriceRange())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .tags(r.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .viewCount(r.getViewCount())
                .favoriteCount(r.getFavoriteCount())
                .build();
    }

    /** 未登录用户返回：隐藏技术参数与热度数据，防止批量爬取 */
    public static RobotSummaryDTO fromPublic(Robot r) {
        return RobotSummaryDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .subtitle(r.getSubtitle())
                .slug(r.getSlug())
                .coverImageUrl(r.getCoverImageUrl())
                .manufacturer(r.getManufacturer() != null ? ManufacturerSummaryDTO.from(r.getManufacturer()) : null)
                .category(r.getCategory() != null ? CategorySummaryDTO.from(r.getCategory()) : null)
                .has3dModel(r.getHas3dModel())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                // 隐藏：modelNumber / payloadKg / reachMm / dof / repeatabilityMm / priceRange / viewCount / favoriteCount
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
