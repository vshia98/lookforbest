package com.lookforbest.repository;

import com.lookforbest.entity.Robot;
import com.lookforbest.entity.User;
import com.lookforbest.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Optional<UserFavorite> findByUserAndRobot(User user, Robot robot);

    boolean existsByUserAndRobot(User user, Robot robot);

    Page<UserFavorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    void deleteByUserAndRobot(User user, Robot robot);
}
