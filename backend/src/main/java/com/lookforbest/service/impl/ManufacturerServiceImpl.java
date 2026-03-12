package com.lookforbest.service.impl;

import com.lookforbest.entity.Manufacturer;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.ManufacturerRepository;
import com.lookforbest.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Manufacturer> findAll(String q, String country, Pageable pageable) {
        return manufacturerRepository.findWithFilters(q, country, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Manufacturer findById(Long id) {
        return manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", id));
    }

    @Override
    @Transactional
    public Manufacturer create(Manufacturer manufacturer) {
        return manufacturerRepository.save(manufacturer);
    }

    @Override
    @Transactional
    public Manufacturer update(Long id, Manufacturer updated) {
        Manufacturer existing = findById(id);
        existing.setName(updated.getName());
        existing.setNameEn(updated.getNameEn());
        existing.setCountry(updated.getCountry());
        existing.setCountryCode(updated.getCountryCode());
        existing.setLogoUrl(updated.getLogoUrl());
        existing.setWebsiteUrl(updated.getWebsiteUrl());
        existing.setDescription(updated.getDescription());
        existing.setDescriptionEn(updated.getDescriptionEn());
        existing.setHeadquarters(updated.getHeadquarters());
        return manufacturerRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!manufacturerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Manufacturer", id);
        }
        manufacturerRepository.deleteById(id);
    }
}
