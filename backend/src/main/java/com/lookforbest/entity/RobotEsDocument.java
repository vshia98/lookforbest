package com.lookforbest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "robots")
@Setting(settingPath = "elasticsearch/robot-settings.json")
@Mapping(mappingPath = "elasticsearch/robot-mappings.json")
public class RobotEsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "english")
    private String nameEn;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Text, analyzer = "english")
    private String descriptionEn;

    @Field(type = FieldType.Keyword)
    private String modelNumber;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String categoryNameEn;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String manufacturerName;

    @Field(type = FieldType.Text, analyzer = "english")
    private String manufacturerNameEn;

    @Field(type = FieldType.Long)
    private Long manufacturerId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Double)
    private BigDecimal payloadKg;

    @Field(type = FieldType.Integer)
    private Integer reachMm;

    @Field(type = FieldType.Integer)
    private Integer dof;

    @Field(type = FieldType.Double)
    private BigDecimal weightKg;

    @Field(type = FieldType.Keyword)
    private String priceRange;

    @Field(type = FieldType.Boolean)
    private Boolean has3dModel;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;

    @Field(type = FieldType.Keyword)
    private String coverImageUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    /** completion suggester field for autocomplete */
    @CompletionField(maxInputLength = 100)
    private List<String> suggest;

    /** 从 Robot JPA 实体转换 */
    public static RobotEsDocument from(Robot robot) {
        List<String> suggestions = new java.util.ArrayList<>();
        if (robot.getName() != null) suggestions.add(robot.getName());
        if (robot.getNameEn() != null) suggestions.add(robot.getNameEn());
        if (robot.getModelNumber() != null) suggestions.add(robot.getModelNumber());
        if (robot.getManufacturer() != null && robot.getManufacturer().getName() != null) {
            suggestions.add(robot.getManufacturer().getName());
        }
        if (robot.getManufacturer() != null && robot.getManufacturer().getNameEn() != null) {
            suggestions.add(robot.getManufacturer().getNameEn());
        }

        return RobotEsDocument.builder()
                .id(String.valueOf(robot.getId()))
                .name(robot.getName())
                .nameEn(robot.getNameEn())
                .slug(robot.getSlug())
                .description(robot.getDescription())
                .descriptionEn(robot.getDescriptionEn())
                .modelNumber(robot.getModelNumber())
                .categoryName(robot.getCategory() != null ? robot.getCategory().getName() : null)
                .categoryNameEn(robot.getCategory() != null ? robot.getCategory().getNameEn() : null)
                .categoryId(robot.getCategory() != null ? robot.getCategory().getId() : null)
                .manufacturerName(robot.getManufacturer() != null ? robot.getManufacturer().getName() : null)
                .manufacturerNameEn(robot.getManufacturer() != null ? robot.getManufacturer().getNameEn() : null)
                .manufacturerId(robot.getManufacturer() != null ? robot.getManufacturer().getId() : null)
                .status(robot.getStatus() != null ? robot.getStatus().name() : null)
                .payloadKg(robot.getPayloadKg())
                .reachMm(robot.getReachMm())
                .dof(robot.getDof() != null ? (int) robot.getDof() : null)
                .weightKg(robot.getWeightKg())
                .priceRange(robot.getPriceRange())
                .has3dModel(robot.getHas3dModel())
                .viewCount(robot.getViewCount())
                .favoriteCount(robot.getFavoriteCount())
                .isFeatured(robot.getIsFeatured())
                .coverImageUrl(robot.getCoverImageUrl())
                .updatedAt(robot.getUpdatedAt())
                .suggest(suggestions)
                .build();
    }
}
