package com.lookforbest.repository;

import com.lookforbest.entity.UserOAuthBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOAuthBindingRepository extends JpaRepository<UserOAuthBinding, Long> {

    Optional<UserOAuthBinding> findByProviderAndProviderUserId(String provider, String providerUserId);
}
