package com.example.dormportal.service;

import com.example.dormportal.model.IncidentReport;
import com.example.dormportal.model.IncidentReportDTO;
import com.example.dormportal.model.Room;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.IncidentRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public IncidentService(IncidentRepository incidentRepository, UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    public List<IncidentReportDTO> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public IncidentReportDTO createIncident(String username, IncidentReportDTO dto) {
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found: " + username));
        Room room = reporter.getRoom();
        if (room == null) {
            throw new IllegalArgumentException("Dormer must be assigned to a room to submit incident reports");
        }

        IncidentReport incident = new IncidentReport();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(incident, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to IncidentReport", e);
        }

        incident.setReporter(reporter);
        incident.setRoom(room);
        incident.setCreatedAt(LocalDateTime.now());

        IncidentReport saved = incidentRepository.save(incident);
        return toDTO(saved);
    }

    private IncidentReportDTO toDTO(IncidentReport incident) {
        IncidentReportDTO dto = new IncidentReportDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, incident);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping IncidentReport to DTO", e);
        }
        dto.setReporterUsername(incident.getReporter().getUsername());
        dto.setRoomNumber(incident.getRoom().getRoomNumber());
        return dto;
    }
}
