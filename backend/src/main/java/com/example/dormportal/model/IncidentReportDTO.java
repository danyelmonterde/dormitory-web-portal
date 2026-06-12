package com.example.dormportal.model;

import java.time.LocalDateTime;

public class IncidentReportDTO {
    private Long id;
    private String reporterUsername;
    private String roomNumber;
    private String remarks;
    private String photoUrls;
    private LocalDateTime createdAt;

    public IncidentReportDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
