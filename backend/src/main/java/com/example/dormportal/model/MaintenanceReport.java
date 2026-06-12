package com.example.dormportal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_reports")
public class MaintenanceReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false, length = 1000)
    private String remarks;

    // Comma-separated list of image base64 data or URLs
    @Column(length = 2000)
    private String photoUrls;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, SCHEDULED, RESOLVED

    private LocalDateTime scheduledDate;

    private String resolverRemarks;

    private LocalDateTime createdAt = LocalDateTime.now();

    public MaintenanceReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

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
