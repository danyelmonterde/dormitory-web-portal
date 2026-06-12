package com.example.dormportal.model;

import java.time.LocalDateTime;

public class MaintenanceReportDTO {
    private Long id;
    private String reporterUsername;
    private String reporterFullName;
    private String reporterContactInfo; // Masked for DORMERs, unmasked for ADMINs
    private String roomNumber;
    private String remarks;
    private String photoUrls;
    private String status;
    private LocalDateTime scheduledDate;
    private String resolverRemarks;
    private LocalDateTime createdAt;

    public MaintenanceReportDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getReporterFullName() { return reporterFullName; }
    public void setReporterFullName(String reporterFullName) { this.reporterFullName = reporterFullName; }

    public String getReporterContactInfo() { return reporterContactInfo; }
    public void setReporterContactInfo(String reporterContactInfo) { this.reporterContactInfo = reporterContactInfo; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getResolverRemarks() { return resolverRemarks; }
    public void setResolverRemarks(String resolverRemarks) { this.resolverRemarks = resolverRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
