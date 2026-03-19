package com.lookforbest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "manufacturers")
@Getter
@Setter
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "country_en", length = 100)
    private String countryEn;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "founded_year")
    private Short foundedYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(length = 300)
    private String headquarters;

    @Column(name = "stock_code", length = 50)
    private String stockCode;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "business_license", length = 500)
    private String businessLicense;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column
    private Boolean status = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
