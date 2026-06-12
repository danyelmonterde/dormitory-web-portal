package com.example.dormportal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestDTO {
    private Long id;
    private String tenantUsername;
    private String tenantFullName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean permanentMoveOut;
    private String remarks;
    private String status;
    private LocalDateTime createdAt;

    public LeaveRequestDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantUsername() { return tenantUsername; }
    public void setTenantUsername(String tenantUsername) { this.tenantUsername = tenantUsername; }

    public String getTenantFullName() { return tenantFullName; }
    public void setTenantFullName(String tenantFullName) { this.tenantFullName = tenantFullName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isPermanentMoveOut() { return permanentMoveOut; }
    public void setPermanentMoveOut(boolean permanentMoveOut) { this.permanentMoveOut = permanentMoveOut; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
