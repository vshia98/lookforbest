package com.lookforbest.repository;

import com.lookforbest.entity.ManufacturerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManufacturerUserRepository extends JpaRepository<ManufacturerUser, Long> {

    /** 查找该用户管理的厂商关联记录 */
    Optional<ManufacturerUser> findByUserId(Long userId);

    List<ManufacturerUser> findByManufacturerId(Long manufacturerId);
}
