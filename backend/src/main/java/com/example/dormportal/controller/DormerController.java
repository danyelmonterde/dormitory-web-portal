package com.example.dormportal.controller;

import com.example.dormportal.model.*;
import com.example.dormportal.repository.UserRepository;
import com.example.dormportal.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dormer")
@PreAuthorize("hasRole('DORMER')")
public class DormerController {

    private final UserService userService;
    private final RoomService roomService;
    private final MaintenanceService maintenanceService;
    private final IncidentService incidentService;
    private final BillService billService;
    private final LeaveService leaveService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DormerController(UserService userService, RoomService roomService, MaintenanceService maintenanceService,
                            IncidentService incidentService, BillService billService, LeaveService leaveService,
                            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roomService = roomService;
        this.maintenanceService = maintenanceService;
        this.incidentService = incidentService;
        this.billService = billService;
        this.leaveService = leaveService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Profile updates
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByUsername(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(Authentication authentication, @RequestBody Map<String, String> body) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (body.containsKey("fullName")) {
            user.setFullName(body.get("fullName"));
        }
        if (body.containsKey("phoneNumber")) {
            user.setPhoneNumber(body.get("phoneNumber"));
        }
        if (body.containsKey("password") && body.get("password") != null && !body.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(userService.toDTO(saved));
    }

    // Room Overview with co-tenants and masking
    @GetMapping("/room")
    public ResponseEntity<Map<String, Object>> getRoomOverview(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Room room = user.getRoom();
        if (room == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "You are not assigned to any room yet.");
            return ResponseEntity.ok(response);
        }

        List<Map<String, String>> coTenants = userRepository.findByRoomId(room.getId()).stream()
                .filter(u -> !u.getUsername().equals(username))
                .map(u -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("fullName", u.getFullName());
                    map.put("email", maskEmail(u.getEmail()));
                    map.put("phoneNumber", maskPhone(u.getPhoneNumber()));
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("roomNumber", room.getRoomNumber());
        response.put("capacity", room.getCapacity());
        response.put("monthlyRent", room.getMonthlyRent());
        response.put("wifiPassword", room.getWifiPassword());
        response.put("coTenants", coTenants);

        return ResponseEntity.ok(response);
    }

    // Maintenance reports
    @GetMapping("/maintenance-reports")
    public ResponseEntity<List<MaintenanceReportDTO>> getMyMaintenanceReports(Authentication authentication) {
        // clients get masked report listings
        return ResponseEntity.ok(maintenanceService.getReportsByReporter(authentication.getName(), false));
    }

    @PostMapping("/maintenance-reports")
    public ResponseEntity<MaintenanceReportDTO> createMaintenanceReport(Authentication authentication, @Valid @RequestBody MaintenanceReportDTO dto) {
        return ResponseEntity.ok(maintenanceService.createReport(authentication.getName(), dto));
    }

    // Incident reports
    @PostMapping("/incident-reports")
    public ResponseEntity<IncidentReportDTO> createIncidentReport(Authentication authentication, @Valid @RequestBody IncidentReportDTO dto) {
        return ResponseEntity.ok(incidentService.createIncident(authentication.getName(), dto));
    }

    // Bills
    @GetMapping("/bills")
    public ResponseEntity<List<BillDTO>> getMyBills(Authentication authentication) {
        return ResponseEntity.ok(billService.getBillsByTenant(authentication.getName()));
    }

    @PostMapping("/bills/{id}/pay")
    public ResponseEntity<BillDTO> payBill(Authentication authentication, @PathVariable Long id) {
        // Ensure user owns the bill
        BillDTO bill = billService.getBillById(id);
        if (!bill.getTenantUsername().equals(authentication.getName())) {
            throw new IllegalArgumentException("Unauthorized payment attempt");
        }
        return ResponseEntity.ok(billService.payBillSimulated(id));
    }

    // Cleaning Booking: appends configured cleaning fee (₱150) to monthly rent profile
    @PostMapping("/cleaning")
    public ResponseEntity<Map<String, String>> bookCleaning(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        double currentFee = user.getCustomCleaningFee() != null ? user.getCustomCleaningFee() : 0.0;
        user.setCustomCleaningFee(currentFee + 150.0); // ₱150 per cleaning request default
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Cleaning request booked successfully! ₱150 has been added to your pending monthly charges."));
    }

    // Leaves
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaves(Authentication authentication) {
        return ResponseEntity.ok(leaveService.getLeavesByTenant(authentication.getName()));
    }

    @PostMapping("/leaves")
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(Authentication authentication, @Valid @RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveService.createLeaveRequest(authentication.getName(), dto));
    }

    // Contact masking helpers
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
