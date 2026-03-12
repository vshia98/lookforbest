package com.lookforbest.dto.response;

import com.lookforbest.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class RobotDetailDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String modelNumber;
    private String slug;
    private String description;
    private String descriptionEn;
    private Short releaseYear;
    private String status;

    private ManufacturerDTO manufacturer;
    private CategoryDTO category;
    private List<DomainDTO> applicationDomains;

    // 技术参数
    private SpecsDTO specs;
    private Map<String, Object> extraSpecs;

    private String priceRange;
    private BigDecimal priceUsdFrom;
    private String coverImageUrl;
    private String model3dUrl;
    private Boolean has3dModel;
    private Boolean hasVideo;

    private List<ImageDTO> images;
    private List<VideoDTO> videos;
    private List<DocumentDTO> documents;

    private Long viewCount;
    private Integer favoriteCount;
    private Boolean isFavorited;
    private Boolean isVerified;

    private List<RobotSummaryDTO> similarRobots;

    @Getter
    @Builder
    public static class SpecsDTO {
        private BigDecimal payloadKg;
        private Integer reachMm;
        private Integer dof;
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
    }

    @Getter
    @Builder
    public static class ImageDTO {
        private Long id;
        private String url;
        private String thumbnailUrl;
        private String imageType;
        private Integer sortOrder;
    }

    @Getter
    @Builder
    public static class VideoDTO {
        private Long id;
        private String title;
        private String url;
        private String thumbnailUrl;
        private Integer durationS;
    }

    @Getter
    @Builder
    public static class DocumentDTO {
        private Long id;
        private String title;
        private String url;
        private String docType;
        private String language;
    }

    public static RobotDetailDTO from(Robot r, List<RobotImage> images, List<RobotVideo> videos,
                                       List<RobotDocument> docs, boolean isFavorited,
                                       List<Robot> similar) {
        return RobotDetailDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .nameEn(r.getNameEn())
                .modelNumber(r.getModelNumber())
                .slug(r.getSlug())
                .description(r.getDescription())
                .descriptionEn(r.getDescriptionEn())
                .releaseYear(r.getReleaseYear())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .manufacturer(r.getManufacturer() != null ? ManufacturerDTO.from(r.getManufacturer()) : null)
                .category(r.getCategory() != null ? CategoryDTO.from(r.getCategory()) : null)
                .applicationDomains(r.getApplicationDomains().stream().map(DomainDTO::from).collect(Collectors.toList()))
                .specs(SpecsDTO.builder()
                        .payloadKg(r.getPayloadKg())
                        .reachMm(r.getReachMm())
                        .dof(r.getDof() != null ? r.getDof().intValue() : null)
                        .repeatabilityMm(r.getRepeatabilityMm())
                        .maxSpeedDegS(r.getMaxSpeedDegS())
                        .weightKg(r.getWeightKg())
                        .ipRating(r.getIpRating())
                        .mounting(r.getMounting())
                        .maxSpeedMs(r.getMaxSpeedMs())
                        .maxLoadKg(r.getMaxLoadKg())
                        .batteryLifeH(r.getBatteryLifeH())
                        .navigationType(r.getNavigationType())
                        .heightMm(r.getHeightMm())
                        .walkingSpeedMs(r.getWalkingSpeedMs())
                        .build())
                .extraSpecs(r.getExtraSpecs())
                .priceRange(r.getPriceRange() != null ? r.getPriceRange().name() : null)
                .priceUsdFrom(r.getPriceUsdFrom())
                .coverImageUrl(r.getCoverImageUrl())
                .model3dUrl(r.getModel3dUrl())
                .has3dModel(r.getHas3dModel())
                .hasVideo(r.getHasVideo())
                .images(images.stream().map(img -> ImageDTO.builder()
                        .id(img.getId())
                        .url(img.getUrl())
                        .thumbnailUrl(img.getThumbnail())
                        .imageType(img.getImageType() != null ? img.getImageType().name() : null)
                        .sortOrder(img.getSortOrder())
                        .build()).collect(Collectors.toList()))
                .videos(videos.stream().map(v -> VideoDTO.builder()
                        .id(v.getId())
                        .title(v.getTitle())
                        .url(v.getUrl())
                        .thumbnailUrl(v.getThumbnail())
                        .durationS(v.getDurationS())
                        .build()).collect(Collectors.toList()))
                .documents(docs.stream().map(d -> DocumentDTO.builder()
                        .id(d.getId())
                        .title(d.getTitle())
                        .url(d.getUrl())
                        .docType(d.getDocType() != null ? d.getDocType().name() : null)
                        .language(d.getLanguage())
                        .build()).collect(Collectors.toList()))
                .viewCount(r.getViewCount())
                .favoriteCount(r.getFavoriteCount())
                .isFavorited(isFavorited)
                .isVerified(r.getIsVerified())
                .similarRobots(similar.stream().map(RobotSummaryDTO::from).collect(Collectors.toList()))
                .build();
    }
}
