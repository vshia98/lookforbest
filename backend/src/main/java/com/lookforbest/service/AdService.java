package com.lookforbest.service;

import com.lookforbest.dto.request.AdCreateRequest;
import com.lookforbest.dto.response.AdPlacementDTO;
import com.lookforbest.entity.AdClickLog;
import com.lookforbest.entity.AdPlacement;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.AdClickLogRepository;
import com.lookforbest.repository.AdPlacementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdPlacementRepository adPlacementRepository;
    private final AdClickLogRepository adClickLogRepository;

    /**
     * 获取指定位置的有效广告列表
     */
    public List<AdPlacementDTO> getActiveAds(String position) {
        try {
            AdPlacement.Position pos = AdPlacement.Position.valueOf(position);
            LocalDate today = LocalDate.now();
            return adPlacementRepository.findActiveAdsByPosition(pos, today)
                    .stream()
                    .map(AdPlacementDTO::from)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * 异步增加展示次数（fire and forget）
     */
    @Async
    public void recordImpression(Long adId) {
        try {
            adPlacementRepository.incrementImpressionCount(adId);
        } catch (Exception e) {
            log.warn("记录广告展示失败 adId={}", adId, e);
        }
    }

    /**
     * 记录点击：写入日志 + 增加点击次数
     */
    @Transactional
    public void recordClick(Long adId, String ip, Long userId) {
        AdClickLog log = new AdClickLog();
        log.setAdId(adId);
        log.setIp(ip);
        log.setUserId(userId);
        adClickLogRepository.save(log);
        adPlacementRepository.incrementClickCount(adId);
    }

    /**
     * 创建广告（管理员）
     */
    @Transactional
    public AdPlacement createAd(AdCreateRequest req) {
        AdPlacement ad = new AdPlacement();
        applyRequest(ad, req);
        return adPlacementRepository.save(ad);
    }

    /**
     * 更新广告（管理员）
     */
    @Transactional
    public AdPlacement updateAd(Long id, AdCreateRequest req) {
        AdPlacement ad = adPlacementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("广告不存在: " + id));
        applyRequest(ad, req);
        return adPlacementRepository.save(ad);
    }

    /**
     * 删除广告（管理员）
     */
    @Transactional
    public void deleteAd(Long id) {
        if (!adPlacementRepository.existsById(id)) {
            throw new ResourceNotFoundException("广告不存在: " + id);
        }
        adPlacementRepository.deleteById(id);
    }

    /**
     * 查询所有广告（管理员）
     */
    public List<AdPlacement> listAllAds() {
        return adPlacementRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * CTR 报告
     */
    public List<Map<String, Object>> getCtrReport() {
        return adPlacementRepository.findCtrStats();
    }

    private void applyRequest(AdPlacement ad, AdCreateRequest req) {
        ad.setTitle(req.getTitle());
        ad.setDescription(req.getDescription());
        ad.setImageUrl(req.getImageUrl());
        ad.setLinkUrl(req.getLinkUrl());
        ad.setPosition(AdPlacement.Position.valueOf(req.getPosition()));
        ad.setAdType(req.getAdType() != null
                ? AdPlacement.AdType.valueOf(req.getAdType())
                : AdPlacement.AdType.banner);
        ad.setPriority(req.getPriority() != null ? req.getPriority() : 0);
        ad.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        ad.setStartDate(req.getStartDate());
        ad.setEndDate(req.getEndDate());
        ad.setManufacturerId(req.getManufacturerId());
    }
}
