package com.example.dormportal.model;

public class BillDTO {
    private Long id;
    private String tenantUsername;
    private String tenantFullName;
    private String roomNumber;
    private String billingMonth;
    private Double baseRent;
    private Double electricityPrev;
    private Double electricityCurr;
    private Double electricityPrice;
    private Double electricityTotal;
    private Double waterPrev;
    private Double waterCurr;
    private Double waterPrice;
    private Double waterTotal;
    private Double cleaningFee;
    private Double grandTotal;
    private boolean paid;
    private String payMongoPaymentId;
    private String invoiceStatus;
    private String meterEvidenceUrl;

    public BillDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantUsername() { return tenantUsername; }
    public void setTenantUsername(String tenantUsername) { this.tenantUsername = tenantUsername; }

    public String getTenantFullName() { return tenantFullName; }
    public void setTenantFullName(String tenantFullName) { this.tenantFullName = tenantFullName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

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

    public Double getElectricityTotal() { return electricityTotal; }
    public void setElectricityTotal(Double electricityTotal) { this.electricityTotal = electricityTotal; }

    public Double getWaterPrev() { return waterPrev; }
    public void setWaterPrev(Double waterPrev) { this.waterPrev = waterPrev; }

    public Double getWaterCurr() { return waterCurr; }
    public void setWaterCurr(Double waterCurr) { this.waterCurr = waterCurr; }

    public Double getWaterPrice() { return waterPrice; }
    public void setWaterPrice(Double waterPrice) { this.waterPrice = waterPrice; }

    public Double getWaterTotal() { return waterTotal; }
    public void setWaterTotal(Double waterTotal) { this.waterTotal = waterTotal; }

    public Double getCleaningFee() { return cleaningFee; }
    public void setCleaningFee(Double cleaningFee) { this.cleaningFee = cleaningFee; }

    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getPayMongoPaymentId() { return payMongoPaymentId; }
    public void setPayMongoPaymentId(String payMongoPaymentId) { this.payMongoPaymentId = payMongoPaymentId; }

    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }

    public String getMeterEvidenceUrl() { return meterEvidenceUrl; }
    public void setMeterEvidenceUrl(String meterEvidenceUrl) { this.meterEvidenceUrl = meterEvidenceUrl; }
}
