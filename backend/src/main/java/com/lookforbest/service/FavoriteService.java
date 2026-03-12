package com.lookforbest.service;

import com.lookforbest.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    void addFavorite(String userEmail, Long robotId);
    void removeFavorite(String userEmail, Long robotId);
    Page<Robot> getFavorites(String userEmail, Pageable pageable);
    boolean isFavorited(String userEmail, Long robotId);
}
