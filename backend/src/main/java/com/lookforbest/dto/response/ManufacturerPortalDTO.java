package com.lookforbest.dto.response;

import com.lookforbest.entity.Manufacturer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ManufacturerPortalDTO {

    private Long id;
    private String name;
    private String nameEn;
    private String country;
    private String countryCode;
    private String logoUrl;
    private String websiteUrl;
    private String description;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String businessLicense;
    private String companySize;
    private Short foundedYear;

    // 统计数据
    private long robotCount;
    private long inquiryCount;
    private long viewCount;

    public static ManufacturerPortalDTO from(Manufacturer m, long robotCount, long inquiryCount, long viewCount) {
        ManufacturerPortalDTO dto = new ManufacturerPortalDTO();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setNameEn(m.getNameEn());
        dto.setCountry(m.getCountry());
        dto.setCountryCode(m.getCountryCode());
        dto.setLogoUrl(m.getLogoUrl());
        dto.setWebsiteUrl(m.getWebsiteUrl());
        dto.setDescription(m.getDescription());
        dto.setIsVerified(m.getIsVerified());
        dto.setContactPerson(m.getContactPerson());
        dto.setContactEmail(m.getContactEmail());
        dto.setContactPhone(m.getContactPhone());
        dto.setBusinessLicense(m.getBusinessLicense());
        dto.setCompanySize(m.getCompanySize());
        dto.setFoundedYear(m.getFoundedYear());
        dto.setVerifiedAt(m.getVerifiedAt());
        dto.setRobotCount(robotCount);
        dto.setInquiryCount(inquiryCount);
        dto.setViewCount(viewCount);
        return dto;
    }
}
