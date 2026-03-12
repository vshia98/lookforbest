package com.lookforbest.repository;

import com.lookforbest.entity.UgcLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UgcLikeRepository extends JpaRepository<UgcLike, Long> {

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, UgcLike.TargetType targetType, Long targetId);

    Optional<UgcLike> findByUserIdAndTargetTypeAndTargetId(Long userId, UgcLike.TargetType targetType, Long targetId);
}
