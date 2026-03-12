package com.lookforbest.dto.response;

import com.lookforbest.entity.ApplicationDomain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DomainDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String slug;

    public static DomainDTO from(ApplicationDomain d) {
        return DomainDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .nameEn(d.getNameEn())
                .slug(d.getSlug())
                .build();
    }
}
