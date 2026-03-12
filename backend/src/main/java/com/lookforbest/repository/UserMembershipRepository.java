package com.lookforbest.repository;

import com.lookforbest.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    Optional<UserMembership> findByUserIdAndStatus(Long userId, UserMembership.Status status);

    Optional<UserMembership> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
