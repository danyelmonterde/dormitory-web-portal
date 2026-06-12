package com.example.dormportal.service;

import com.example.dormportal.model.LeaveRequest;
import com.example.dormportal.model.LeaveRequestDTO;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.LeaveRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;

    public LeaveService(LeaveRepository leaveRepository, UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
    }

    public List<LeaveRequestDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getLeavesByTenant(String username) {
        User tenant = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + username));
        return leaveRepository.findByTenantId(tenant.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LeaveRequestDTO createLeaveRequest(String username, LeaveRequestDTO dto) {
        User tenant = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + username));

        LeaveRequest leave = new LeaveRequest();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(leave, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to LeaveRequest", e);
        }

        leave.setTenant(tenant);
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRepository.save(leave);
        return toDTO(saved);
    }

    public LeaveRequestDTO updateLeaveStatus(Long id, String status) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with ID: " + id));

        leave.setStatus(status);
        LeaveRequest saved = leaveRepository.save(leave);
        return toDTO(saved);
    }

    private LeaveRequestDTO toDTO(LeaveRequest leave) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, leave);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping LeaveRequest to DTO", e);
        }
        dto.setTenantUsername(leave.getTenant().getUsername());
        dto.setTenantFullName(leave.getTenant().getFullName());
        return dto;
    }
}
