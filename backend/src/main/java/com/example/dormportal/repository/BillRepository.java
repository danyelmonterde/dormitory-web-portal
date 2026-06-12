package com.example.dormportal.repository;

import com.example.dormportal.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByTenantId(Long tenantId);
    Optional<Bill> findByTenantIdAndBillingMonth(Long tenantId, String billingMonth);
}
