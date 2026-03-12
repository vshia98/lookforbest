package com.lookforbest.service;

import com.lookforbest.entity.Manufacturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManufacturerService {
    Page<Manufacturer> findAll(String q, String country, Pageable pageable);
    Manufacturer findById(Long id);
    Manufacturer create(Manufacturer manufacturer);
    Manufacturer update(Long id, Manufacturer manufacturer);
    void delete(Long id);
}
