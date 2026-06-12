package com.example.dormportal.service;

import com.example.dormportal.model.MaintenanceReport;
import com.example.dormportal.model.MaintenanceReportDTO;
import com.example.dormportal.model.Room;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.MaintenanceRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final UserRepository userRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, UserRepository userRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.userRepository = userRepository;
    }

    public List<MaintenanceReportDTO> getAllReports(boolean isAdmin) {
        return maintenanceRepository.findAll().stream()
                .map(r -> toDTO(r, isAdmin))
                .collect(Collectors.toList());
    }

    public List<MaintenanceReportDTO> getReportsByReporter(String username, boolean isAdmin) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return maintenanceRepository.findByReporterId(reporter.getId()).stream()
                .map(r -> toDTO(r, isAdmin))
                .collect(Collectors.toList());
    }

    public MaintenanceReportDTO getReportById(Long id, boolean isAdmin) {
        MaintenanceReport report = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));
        return toDTO(report, isAdmin);
    }

    public MaintenanceReportDTO createReport(String username, MaintenanceReportDTO dto) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found: " + username));
        Room room = reporter.getRoom();
        if (room == null) {
            throw new IllegalArgumentException("Dormer must be assigned to a room to submit maintenance reports");
        }

        MaintenanceReport report = new MaintenanceReport();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(report, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to MaintenanceReport", e);
        }

        report.setReporter(reporter);
        report.setRoom(room);
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());

        MaintenanceReport saved = maintenanceRepository.save(report);
        return toDTO(saved, false); // Client creating their own: masking doesn't matter much but false is safe
    }

    public MaintenanceReportDTO updateReportStatus(Long id, MaintenanceReportDTO dto) {
        MaintenanceReport report = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));

        if (dto.getStatus() != null) {
            report.setStatus(dto.getStatus());
        }
        if (dto.getScheduledDate() != null) {
            report.setScheduledDate(dto.getScheduledDate());
        }
        if (dto.getResolverRemarks() != null) {
            report.setResolverRemarks(dto.getResolverRemarks());
        }

        MaintenanceReport saved = maintenanceRepository.save(report);
        return toDTO(saved, true); // Admin flow: isAdmin = true
    }

    public void deleteReport(Long id) {
        maintenanceRepository.deleteById(id);
    }

    private MaintenanceReportDTO toDTO(MaintenanceReport report, boolean isAdmin) {
        MaintenanceReportDTO dto = new MaintenanceReportDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, report);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping MaintenanceReport to DTO", e);
        }
        dto.setReporterUsername(report.getReporter().getUsername());
        dto.setReporterFullName(report.getReporter().getFullName());
        dto.setRoomNumber(report.getRoom().getRoomNumber());

        // Mask contact info for clients/dormers
        String contactInfo = report.getReporter().getEmail() + " / " + report.getReporter().getPhoneNumber();
        if (!isAdmin) {
            contactInfo = maskEmail(report.getReporter().getEmail()) + " / " + maskPhone(report.getReporter().getPhoneNumber());
        }
        dto.setReporterContactInfo(contactInfo);

        return dto;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        if (parts[0].length() <= 1) {
            return parts[0] + "***@" + parts[1];
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        int length = phone.length();
        return phone.substring(0, 5) + "***" + phone.substring(length - 4);
    }
}
