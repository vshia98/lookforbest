package com.lookforbest.repository;

import com.lookforbest.entity.Manufacturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    @Query("""
        SELECT m FROM Manufacturer m
        WHERE (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%',:q,'%'))
                          OR LOWER(m.nameEn) LIKE LOWER(CONCAT('%',:q,'%')))
          AND (:country IS NULL OR m.countryCode = :country)
          AND m.status = true
        """)
    Page<Manufacturer> findWithFilters(
            @Param("q") String q,
            @Param("country") String country,
            Pageable pageable
    );
}
