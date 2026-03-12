package com.lookforbest.dto.response;

import com.lookforbest.entity.Manufacturer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManufacturerDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String country;
    private String countryCode;
    private String logoUrl;
    private String websiteUrl;
    private Short foundedYear;
    private String description;
    private String descriptionEn;
    private String headquarters;
    private Boolean isVerified;

    public static ManufacturerDTO from(Manufacturer m) {
        return ManufacturerDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .nameEn(m.getNameEn())
                .country(m.getCountry())
                .countryCode(m.getCountryCode())
                .logoUrl(m.getLogoUrl())
                .websiteUrl(m.getWebsiteUrl())
                .foundedYear(m.getFoundedYear())
                .description(m.getDescription())
                .descriptionEn(m.getDescriptionEn())
                .headquarters(m.getHeadquarters())
                .isVerified(m.getIsVerified())
                .build();
    }
}
