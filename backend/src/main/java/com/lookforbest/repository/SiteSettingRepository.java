package com.lookforbest.repository;

import com.lookforbest.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {

    List<SiteSetting> findBySettingKeyIn(List<String> keys);
}
