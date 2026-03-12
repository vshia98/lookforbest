package com.lookforbest.repository;

import com.lookforbest.entity.ApplicationDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationDomainRepository extends JpaRepository<ApplicationDomain, Long> {

    List<ApplicationDomain> findAllByOrderByNameAsc();
}
