package com.example.dormportal.service;

import com.example.dormportal.model.Bill;
import com.example.dormportal.model.BillDTO;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.BillRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public BillService(BillRepository billRepository, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

    public List<BillDTO> getAllBills() {
        return billRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BillDTO> getBillsByTenant(String username) {
        User tenant = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + username));
        return billRepository.findByTenantId(tenant.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BillDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + id));
        return toDTO(bill);
    }

    public BillDTO createBill(BillDTO dto) {
        User tenant = userRepository.findByUsername(dto.getTenantUsername())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + dto.getTenantUsername()));

        // Check if there is an existing bill for this tenant and month
        billRepository.findByTenantIdAndBillingMonth(tenant.getId(), dto.getBillingMonth())
                .ifPresent(b -> {
                    throw new IllegalArgumentException("Bill already exists for tenant " + tenant.getUsername() + " in month " + dto.getBillingMonth());
                });

        Bill bill = new Bill();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(bill, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to Bill", e);
        }

        bill.setTenant(tenant);
        // Base rent is taken from room monthlyRent if not specified
        if (dto.getBaseRent() == null || dto.getBaseRent() == 0.0) {
            if (tenant.getRoom() != null) {
                bill.setBaseRent(tenant.getRoom().getMonthlyRent());
            } else {
                bill.setBaseRent(0.0);
            }
        }

        // Incorporate cleaning request fee from user profile if there is one
        double pendingCleaning = tenant.getCustomCleaningFee() != null ? tenant.getCustomCleaningFee() : 0.0;
        bill.setCleaningFee(pendingCleaning);
        
        // Reset user cleaning request fee on profile once billed
        tenant.setCustomCleaningFee(0.0);
        userRepository.save(tenant);

        bill.setPaid(false);
        bill.setInvoiceStatus("PENDING");

        Bill saved = billRepository.save(bill);
        return toDTO(saved);
    }

    public BillDTO updateBill(Long id, BillDTO dto) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + id));

        bill.setElectricityPrev(dto.getElectricityPrev());
        bill.setElectricityCurr(dto.getElectricityCurr());
        bill.setElectricityPrice(dto.getElectricityPrice());
        bill.setWaterPrev(dto.getWaterPrev());
        bill.setWaterCurr(dto.getWaterCurr());
        bill.setWaterPrice(dto.getWaterPrice());
        bill.setMeterEvidenceUrl(dto.getMeterEvidenceUrl());
        bill.setBaseRent(dto.getBaseRent());
        bill.setBillingMonth(dto.getBillingMonth());

        Bill saved = billRepository.save(bill);
        return toDTO(saved);
    }

    public void deleteBill(Long id) {
        billRepository.deleteById(id);
    }

    public BillDTO payBillSimulated(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + id));

        // Generate simulated PayMongo reference id
        String simulatedPaymentId = "paymongo_ref_" + System.currentTimeMillis();
        bill.setPayMongoPaymentId(simulatedPaymentId);
        
        // Webhook will handle setting PAID, but we can set it here too as a fallback, 
        // or trigger the webhook handler immediately to simulate the asynchronous loop
        return toDTO(billRepository.save(bill));
    }

    public void processPayMongoWebhook(String paymentId, String status, Long billId) {
        Bill bill;
        if (billId != null) {
            bill = billRepository.findById(billId)
                    .orElseThrow(() -> new IllegalArgumentException("Bill not found with ID: " + billId));
        } else {
            // Find by payment reference
            bill = billRepository.findAll().stream()
                    .filter(b -> paymentId.equals(b.getPayMongoPaymentId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Bill not found with payment ID: " + paymentId));
        }

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            bill.setPaid(true);
            bill.setInvoiceStatus("PAID");
            bill.setPayMongoPaymentId(paymentId);
            billRepository.save(bill);
            System.out.println("Bill ID " + bill.getId() + " marked as PAID via PayMongo Webhook (ref: " + paymentId + ")");
        }
    }

    private BillDTO toDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, bill);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping Bill to DTO", e);
        }
        dto.setTenantUsername(bill.getTenant().getUsername());
        dto.setTenantFullName(bill.getTenant().getFullName());
        dto.setRoomNumber(bill.getTenant().getRoom() != null ? bill.getTenant().getRoom().getRoomNumber() : "N/A");
        dto.setElectricityTotal(bill.getElectricityTotal());
        dto.setWaterTotal(bill.getWaterTotal());
        dto.setGrandTotal(bill.getGrandTotal());
        return dto;
    }
}
