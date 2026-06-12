package com.example.dormportal.controller;

import com.example.dormportal.model.*;
import com.example.dormportal.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoomService roomService;
    private final MaintenanceService maintenanceService;
    private final IncidentService incidentService;
    private final BillService billService;
    private final LeaveService leaveService;
    private final FacilityService facilityService;

    public AdminController(UserService userService, RoomService roomService, MaintenanceService maintenanceService,
                           IncidentService incidentService, BillService billService, LeaveService leaveService,
                           FacilityService facilityService) {
        this.userService = userService;
        this.roomService = roomService;
        this.maintenanceService = maintenanceService;
        this.incidentService = incidentService;
        this.billService = billService;
        this.leaveService = leaveService;
        this.facilityService = facilityService;
    }

    // User CRUD
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // Room CRUD
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Map<String, String>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
    }

    // Facility CRUD
    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.getFacilityById(id));
    }

    @PostMapping("/facilities")
    public ResponseEntity<Facility> createFacility(@Valid @RequestBody Facility facility) {
        return ResponseEntity.ok(facilityService.createFacility(facility));
    }

    @PutMapping("/facilities/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable Long id, @Valid @RequestBody Facility facility) {
        return ResponseEntity.ok(facilityService.updateFacility(id, facility));
    }

    @DeleteMapping("/facilities/{id}")
    public ResponseEntity<Map<String, String>> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.ok(Map.of("message", "Facility deleted successfully"));
    }

    // Maintenance reports
    @GetMapping("/maintenance-reports")
    public ResponseEntity<List<MaintenanceReportDTO>> getAllMaintenanceReports() {
        // Admin gets unmasked details
        return ResponseEntity.ok(maintenanceService.getAllReports(true));
    }

    @PutMapping("/maintenance-reports/{id}")
    public ResponseEntity<MaintenanceReportDTO> updateMaintenanceReport(@PathVariable Long id, @RequestBody MaintenanceReportDTO dto) {
        return ResponseEntity.ok(maintenanceService.updateReportStatus(id, dto));
    }

    // Incident reports
    @GetMapping("/incident-reports")
    public ResponseEntity<List<IncidentReportDTO>> getAllIncidentReports() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    // Billing management
    @GetMapping("/bills")
    public ResponseEntity<List<BillDTO>> getAllBills() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    @PostMapping("/bills")
    public ResponseEntity<BillDTO> createBill(@Valid @RequestBody BillDTO billDTO) {
        return ResponseEntity.ok(billService.createBill(billDTO));
    }

    @PutMapping("/bills/{id}")
    public ResponseEntity<BillDTO> updateBill(@PathVariable Long id, @Valid @RequestBody BillDTO billDTO) {
        return ResponseEntity.ok(billService.updateBill(id, billDTO));
    }

    @DeleteMapping("/bills/{id}")
    public ResponseEntity<Map<String, String>> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok(Map.of("message", "Bill deleted successfully"));
    }

    // Leave Requests management
    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PatchMapping("/leaves/{id}")
    public ResponseEntity<LeaveRequestDTO> updateLeaveStatus(@PathVariable Long id, @RequestBody Map<String, String> statusBody) {
        String status = statusBody.get("status");
        if (status == null) {
            throw new IllegalArgumentException("Status field 'status' is required");
        }
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, status));
    }
}
