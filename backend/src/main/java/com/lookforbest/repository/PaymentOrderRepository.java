package com.lookforbest.repository;

import com.lookforbest.entity.PaymentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PaymentOrder> findByOrderNo(String orderNo);

    Page<PaymentOrder> findByStatus(PaymentOrder.Status status, Pageable pageable);
}
