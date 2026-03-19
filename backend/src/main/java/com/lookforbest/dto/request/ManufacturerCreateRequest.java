package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManufacturerCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String nameEn;

    @NotBlank
    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String countryEn;

    @Size(max = 10)
    private String countryCode;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String websiteUrl;
    private Short foundedYear;
    private String description;
    private String descriptionEn;
    private String headquarters;
}
