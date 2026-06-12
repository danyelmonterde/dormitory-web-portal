package com.example.dormportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private User tenant;

    @Column(nullable = false)
    private String billingMonth; // e.g. "June 2026" or "2026-06"

    private Double baseRent = 0.0;

    private Double electricityPrev = 0.0;
    private Double electricityCurr = 0.0;
    private Double electricityPrice = 12.0; // ₱12/kWh default

    private Double waterPrev = 0.0;
    private Double waterCurr = 0.0;
    private Double waterPrice = 45.0; // ₱45/cbm default

    private Double cleaningFee = 0.0;

    private boolean paid = false;

    private String payMongoPaymentId;

    private String invoiceStatus = "PENDING"; // PENDING, PAID, OVERDUE

    private String meterEvidenceUrl; // Photos/PDF upload

    public Bill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getTenant() { return tenant; }
    public void setTenant(User tenant) { this.tenant = tenant; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public Double getBaseRent() { return baseRent; }
    public void setBaseRent(Double baseRent) { this.baseRent = baseRent; }

    public Double getElectricityPrev() { return electricityPrev; }
    public void setElectricityPrev(Double electricityPrev) { this.electricityPrev = electricityPrev; }

    public Double getElectricityCurr() { return electricityCurr; }
    public void setElectricityCurr(Double electricityCurr) { this.electricityCurr = electricityCurr; }

    public Double getElectricityPrice() { return electricityPrice; }
    public void setElectricityPrice(Double electricityPrice) { this.electricityPrice = electricityPrice; }

    public Double getWaterPrev() { return waterPrev; }
    public void setWaterPrev(Double waterPrev) { this.waterPrev = waterPrev; }

    public Double getWaterCurr() { return waterCurr; }
    public void setWaterCurr(Double waterCurr) { this.waterCurr = waterCurr; }

    public Double getWaterPrice() { return waterPrice; }
    public void setWaterPrice(Double waterPrice) { this.waterPrice = waterPrice; }

    public Double getCleaningFee() { return cleaningFee; }
    public void setCleaningFee(Double cleaningFee) { this.cleaningFee = cleaningFee; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getPayMongoPaymentId() { return payMongoPaymentId; }
    public void setPayMongoPaymentId(String payMongoPaymentId) { this.payMongoPaymentId = payMongoPaymentId; }

    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }

    public String getMeterEvidenceUrl() { return meterEvidenceUrl; }
    public void setMeterEvidenceUrl(String meterEvidenceUrl) { this.meterEvidenceUrl = meterEvidenceUrl; }

    // Helper to calculate total electricity cost
    public Double getElectricityTotal() {
        if (electricityCurr >= electricityPrev) {
            return (electricityCurr - electricityPrev) * electricityPrice;
        }
        return 0.0;
    }

    // Helper to calculate total water cost
    public Double getWaterTotal() {
        if (waterCurr >= waterPrev) {
            return (waterCurr - waterPrev) * waterPrice;
        }
        return 0.0;
    }

    // Helper to calculate grand total
    public Double getGrandTotal() {
        return baseRent + getElectricityTotal() + getWaterTotal() + cleaningFee;
    }
}
