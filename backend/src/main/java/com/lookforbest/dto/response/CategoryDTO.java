package com.lookforbest.dto.response;

import com.lookforbest.entity.RobotCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String nameEn;
    private String slug;
    private String icon;
    private String description;
    private Integer sortOrder;
    private List<CategoryDTO> children;

    public static CategoryDTO from(RobotCategory c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .name(c.getName())
                .nameEn(c.getNameEn())
                .slug(c.getSlug())
                .icon(c.getIcon())
                .description(c.getDescription())
                .sortOrder(c.getSortOrder())
                .build();
    }
}
