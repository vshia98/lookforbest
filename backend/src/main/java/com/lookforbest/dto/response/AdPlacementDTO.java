package com.lookforbest.dto.response;

import com.lookforbest.entity.AdPlacement;
import lombok.Data;

/**
 * 广告位展示DTO（对外隐藏统计数据）
 */
@Data
public class AdPlacementDTO {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private String adType;
    private Integer priority;
    private Long manufacturerId;

    public static AdPlacementDTO from(AdPlacement ad) {
        AdPlacementDTO dto = new AdPlacementDTO();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setImageUrl(ad.getImageUrl());
        dto.setLinkUrl(ad.getLinkUrl());
        dto.setPosition(ad.getPosition().name());
        dto.setAdType(ad.getAdType().name());
        dto.setPriority(ad.getPriority());
        dto.setManufacturerId(ad.getManufacturerId());
        return dto;
    }
}
