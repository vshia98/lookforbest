package com.lookforbest.repository;

import com.lookforbest.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RobotRepository extends JpaRepository<Robot, Long> {

    long countByStatus(Robot.RobotStatus status);

    @Query("SELECT SUM(r.viewCount) FROM Robot r")
    Long sumViewCount();

    @Query("SELECT SUM(r.favoriteCount) FROM Robot r")
    Long sumFavoriteCount();

    @Query("SELECT new map(c.name as name, COUNT(r) as count) FROM Robot r JOIN r.category c GROUP BY c.id, c.name ORDER BY COUNT(r) DESC")
    List<Map<String, Object>> countByCategory();

    List<Robot> findTop10ByOrderByViewCountDesc();

    Optional<Robot> findBySlug(String slug);

    @Query("""
        SELECT DISTINCT r FROM Robot r
        LEFT JOIN r.applicationDomains d
        WHERE (:q IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%',:q,'%'))
                           OR LOWER(r.nameEn) LIKE LOWER(CONCAT('%',:q,'%'))
                           OR LOWER(r.modelNumber) LIKE LOWER(CONCAT('%',:q,'%')))
          AND (:categoryId IS NULL OR r.category.id = :categoryId)
          AND (:manufacturerId IS NULL OR r.manufacturer.id = :manufacturerId)
          AND (:domainIds IS NULL OR d.id IN :domainIds)
          AND (:payloadMin IS NULL OR r.payloadKg >= :payloadMin)
          AND (:payloadMax IS NULL OR r.payloadKg <= :payloadMax)
          AND (:reachMin IS NULL OR r.reachMm >= :reachMin)
          AND (:reachMax IS NULL OR r.reachMm <= :reachMax)
          AND (:dofMin IS NULL OR r.dof >= :dofMin)
          AND (:dofMax IS NULL OR r.dof <= :dofMax)
          AND (:has3dModel IS NULL OR r.has3dModel = :has3dModel)
          AND (:status IS NULL OR r.status = :status)
        """)
    Page<Robot> findWithFilters(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("manufacturerId") Long manufacturerId,
            @Param("domainIds") List<Long> domainIds,
            @Param("payloadMin") BigDecimal payloadMin,
            @Param("payloadMax") BigDecimal payloadMax,
            @Param("reachMin") Integer reachMin,
            @Param("reachMax") Integer reachMax,
            @Param("dofMin") Integer dofMin,
            @Param("dofMax") Integer dofMax,
            @Param("has3dModel") Boolean has3dModel,
            @Param("status") Robot.RobotStatus status,
            Pageable pageable
    );

    List<Robot> findTop6ByCategoryAndIdNotOrderByViewCountDesc(
            com.lookforbest.entity.RobotCategory category, Long id);

    /** 返回所有机器人的 slug 和 updatedAt，用于 sitemap 生成 */
    @Query("SELECT r.slug, r.updatedAt FROM Robot r WHERE r.status = com.lookforbest.entity.Robot.RobotStatus.active ORDER BY r.updatedAt DESC")
    List<Object[]> findAllSlugAndUpdatedAt();

    /** 厂商门户：统计该厂商的机器人数量 */
    long countByManufacturerId(Long manufacturerId);

    /** 厂商门户：统计该厂商所有机器人的总浏览量 */
    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM Robot r WHERE r.manufacturer.id = :manufacturerId")
    long sumViewCountByManufacturerId(@Param("manufacturerId") Long manufacturerId);
}
