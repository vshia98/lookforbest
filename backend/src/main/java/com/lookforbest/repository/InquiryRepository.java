package com.lookforbest.repository;

import com.lookforbest.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByUserId(Long userId, Pageable pageable);

    Page<Inquiry> findByManufacturerId(Long manufacturerId, Pageable pageable);

    Page<Inquiry> findByManufacturerIdAndStatus(Long manufacturerId, Inquiry.InquiryStatus status, Pageable pageable);

    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(Inquiry.InquiryStatus status);

    /** 厂商门户：统计该厂商的询价数量 */
    long countByManufacturerId(Long manufacturerId);
}
