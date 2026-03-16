package com.lookforbest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "robots")
@Getter
@Setter
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private RobotCategory category;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(name = "name_en", length = 300)
    private String nameEn;

    @Column(length = 500)
    private String subtitle;

    @Column(name = "model_number", length = 200)
    private String modelNumber;

    @Column(unique = true, nullable = false, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "application_scenarios", columnDefinition = "TEXT")
    private String applicationScenarios;

    @Column(columnDefinition = "TEXT")
    private String advantages;

    @Column(name = "release_year")
    private Short releaseYear;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RobotStatus status = RobotStatus.active;

    // 技术参数
    private Byte dof;

    @Column(name = "payload_kg", precision = 10, scale = 3)
    private BigDecimal payloadKg;

    @Column(name = "reach_mm")
    private Integer reachMm;

    @Column(name = "repeatability_mm", precision = 8, scale = 4)
    private BigDecimal repeatabilityMm;

    @Column(name = "max_speed_deg_s", precision = 10, scale = 2)
    private BigDecimal maxSpeedDegS;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "ip_rating", length = 20)
    private String ipRating;

    @Column(length = 100)
    private String mounting;

    @Column(name = "max_speed_m_s", precision = 6, scale = 3)
    private BigDecimal maxSpeedMs;

    @Column(name = "max_load_kg", precision = 10, scale = 2)
    private BigDecimal maxLoadKg;

    @Column(name = "battery_life_h", precision = 5, scale = 1)
    private BigDecimal batteryLifeH;

    @Column(name = "navigation_type", length = 200)
    private String navigationType;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Column(name = "walking_speed_m_s", precision = 5, scale = 3)
    private BigDecimal walkingSpeedMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_specs", columnDefinition = "json")
    private Map<String, Object> extraSpecs;

    @Column(name = "price_range", length = 100)
    private String priceRange = "inquiry";

    @Column(name = "price_usd_from", precision = 12, scale = 2)
    private BigDecimal priceUsdFrom;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_images", columnDefinition = "json")
    private List<Map<String, String>> contentImages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "video_urls", columnDefinition = "json")
    private List<String> videoUrls;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "robot_tag_mappings",
        joinColumns = @JoinColumn(name = "robot_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<RobotTag> tags = new HashSet<>();

    @Column(name = "is_open_source", length = 20)
    private String isOpenSource;

    @Column(name = "listed_date")
    private LocalDate listedDate;

    @Column(name = "model_3d_url", length = 500)
    private String model3dUrl;

    @Column(name = "model_3d_preview", length = 500)
    private String model3dPreview;

    @Column(name = "has_3d_model")
    private Boolean has3dModel = false;

    @Column(name = "has_video")
    private Boolean hasVideo = false;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "compare_count")
    private Integer compareCount = 0;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "robot_application_domains",
        joinColumns = @JoinColumn(name = "robot_id"),
        inverseJoinColumns = @JoinColumn(name = "domain_id")
    )
    private Set<ApplicationDomain> applicationDomains = new HashSet<>();

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

    public enum RobotStatus { active, discontinued, upcoming }
}
