package com.lookforbest.service.impl;

import com.lookforbest.config.CacheConfig;
import com.lookforbest.entity.ApplicationDomain;
import com.lookforbest.entity.Manufacturer;
import com.lookforbest.entity.Robot;
import com.lookforbest.entity.RobotCategory;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.ApplicationDomainRepository;
import com.lookforbest.repository.ManufacturerRepository;
import com.lookforbest.repository.RobotCategoryRepository;
import com.lookforbest.repository.RobotRepository;
import com.lookforbest.service.ElasticsearchSyncService;
import com.lookforbest.service.RobotService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {

    private final RobotRepository robotRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final RobotCategoryRepository categoryRepository;
    private final ApplicationDomainRepository domainRepository;
    private final ElasticsearchSyncService elasticsearchSyncService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ROBOTS_LIST, key = "#root.methodName + '_' + #q + '_' + #categoryId + '_' + #manufacturerId + '_' + #sort + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Robot> findAll(String q, Long categoryId, Long manufacturerId, List<Long> domainIds,
                                BigDecimal payloadMin, BigDecimal payloadMax,
                                Integer reachMin, Integer reachMax,
                                Integer dofMin, Integer dofMax,
                                Boolean has3dModel, String status, String sort, Pageable pageable) {
        Robot.RobotStatus robotStatus = null;
        if (status != null) {
            try {
                robotStatus = Robot.RobotStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }

        Sort sortSpec = switch (sort == null ? "relevance" : sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "releaseYear");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "isFeatured", "sortOrder", "viewCount");
        };

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortSpec);

        return robotRepository.findWithFilters(
                q, categoryId, manufacturerId,
                (domainIds == null || domainIds.isEmpty()) ? null : domainIds,
                payloadMin, payloadMax, reachMin, reachMax,
                dofMin, dofMax, has3dModel, robotStatus, sortedPageable
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.ROBOTS_DETAIL, key = "#id")
    public Robot findById(Long id) {
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Robot", id));
        robot.setViewCount(robot.getViewCount() + 1);
        return robotRepository.save(robot);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ROBOTS_DETAIL, key = "'slug_' + #slug")
    public Robot findBySlug(String slug) {
        return robotRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Robot slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ROBOTS_SIMILAR, key = "#id + '_' + #size")
    public List<Robot> findSimilar(Long id, int size) {
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Robot", id));
        return robotRepository.findTop6ByCategoryAndIdNotOrderByViewCountDesc(robot.getCategory(), id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.ROBOTS_LIST, CacheConfig.ROBOTS_SIMILAR}, allEntries = true)
    public Robot create(Robot robot) {
        if (robot.getSlug() == null || robot.getSlug().isBlank()) {
            robot.setSlug(generateSlug(robot.getManufacturer().getName() + "-" + robot.getName()));
        }
        Robot saved = robotRepository.save(robot);
        elasticsearchSyncService.syncRobot(saved);
        return saved;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROBOTS_DETAIL, key = "#id"),
        @CacheEvict(value = CacheConfig.ROBOTS_LIST, allEntries = true),
        @CacheEvict(value = CacheConfig.ROBOTS_SIMILAR, allEntries = true)
    })
    public Robot update(Long id, Robot updated) {
        Robot existing = robotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Robot", id));
        existing.setName(updated.getName());
        existing.setNameEn(updated.getNameEn());
        existing.setDescription(updated.getDescription());
        existing.setDescriptionEn(updated.getDescriptionEn());
        existing.setPayloadKg(updated.getPayloadKg());
        existing.setReachMm(updated.getReachMm());
        existing.setDof(updated.getDof());
        existing.setRepeatabilityMm(updated.getRepeatabilityMm());
        existing.setMaxSpeedDegS(updated.getMaxSpeedDegS());
        existing.setWeightKg(updated.getWeightKg());
        existing.setIpRating(updated.getIpRating());
        existing.setMounting(updated.getMounting());
        existing.setStatus(updated.getStatus());
        existing.setPriceRange(updated.getPriceRange());
        existing.setPriceUsdFrom(updated.getPriceUsdFrom());
        existing.setCoverImageUrl(updated.getCoverImageUrl());
        existing.setExtraSpecs(updated.getExtraSpecs());
        if (updated.getApplicationDomains() != null) {
            existing.setApplicationDomains(updated.getApplicationDomains());
        }
        Robot saved = robotRepository.save(existing);
        elasticsearchSyncService.syncRobot(saved);
        return saved;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.ROBOTS_DETAIL, key = "#id"),
        @CacheEvict(value = CacheConfig.ROBOTS_LIST, allEntries = true),
        @CacheEvict(value = CacheConfig.ROBOTS_SIMILAR, allEntries = true)
    })
    public void delete(Long id) {
        if (!robotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Robot", id);
        }
        robotRepository.deleteById(id);
        elasticsearchSyncService.deleteRobot(id);
    }

    public Robot buildFromRequest(com.lookforbest.dto.request.RobotCreateRequest req) {
        Manufacturer manufacturer = manufacturerRepository.findById(req.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", req.getManufacturerId()));
        RobotCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));

        Robot robot = new Robot();
        robot.setManufacturer(manufacturer);
        robot.setCategory(category);
        robot.setName(req.getName());
        robot.setNameEn(req.getNameEn());
        robot.setModelNumber(req.getModelNumber());
        robot.setDescription(req.getDescription());
        robot.setDescriptionEn(req.getDescriptionEn());
        robot.setReleaseYear(req.getReleaseYear());
        robot.setDof(req.getDof());
        robot.setPayloadKg(req.getPayloadKg());
        robot.setReachMm(req.getReachMm());
        robot.setRepeatabilityMm(req.getRepeatabilityMm());
        robot.setMaxSpeedDegS(req.getMaxSpeedDegS());
        robot.setWeightKg(req.getWeightKg());
        robot.setIpRating(req.getIpRating());
        robot.setMounting(req.getMounting());
        robot.setMaxSpeedMs(req.getMaxSpeedMs());
        robot.setMaxLoadKg(req.getMaxLoadKg());
        robot.setBatteryLifeH(req.getBatteryLifeH());
        robot.setNavigationType(req.getNavigationType());
        robot.setHeightMm(req.getHeightMm());
        robot.setWalkingSpeedMs(req.getWalkingSpeedMs());
        robot.setExtraSpecs(req.getExtraSpecs());
        robot.setCoverImageUrl(req.getCoverImageUrl());
        if (req.getPriceRange() != null) {
            robot.setPriceRange(Robot.PriceRange.valueOf(req.getPriceRange()));
        }
        if (req.getStatus() != null) {
            robot.setStatus(Robot.RobotStatus.valueOf(req.getStatus()));
        }
        if (req.getApplicationDomainIds() != null) {
            Set<ApplicationDomain> domains = new HashSet<>(
                    domainRepository.findAllById(req.getApplicationDomainIds()));
            robot.setApplicationDomains(domains);
        }
        robot.setSlug(generateSlug(manufacturer.getName() + "-" + req.getName()));
        return robot;
    }

    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        // 保证唯一性
        String base = slug;
        int suffix = 1;
        while (robotRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }
}
