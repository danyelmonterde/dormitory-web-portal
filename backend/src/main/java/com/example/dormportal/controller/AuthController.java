package com.example.dormportal.controller;

import com.example.dormportal.config.JwtService;
import com.example.dormportal.model.LoginRequest;
import com.example.dormportal.model.LoginResponse;
import com.example.dormportal.model.User;
import com.example.dormportal.repository.UserRepository;
import com.example.dormportal.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BillService billService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, BillService billService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.billService = billService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUsername()));
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole().name()));
        } else {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            System.out.println("Simulating forgot-password email to " + email + ". Reset link: http://localhost:4200/reset-password?token=simulated_token_" + user.getUsername());
        });

        return ResponseEntity.ok(Map.of("message", "If the email is registered, a password reset link has been simulated in logs and email sent."));
    }

    @PostMapping("/paymongo-webhook")
    public ResponseEntity<Map<String, String>> payMongoWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Received PayMongo Webhook: " + payload);
        
        // Extract payment details from PayMongo simulated body
        // Body format: { "paymentId": "...", "status": "SUCCESS", "billId": 123 }
        try {
            String paymentId = (String) payload.get("paymentId");
            String status = (String) payload.get("status");
            Number billIdNum = (Number) payload.get("billId");
            Long billId = billIdNum != null ? billIdNum.longValue() : null;

            if (paymentId != null && status != null) {
                billService.processPayMongoWebhook(paymentId, status, billId);
                return ResponseEntity.ok(Map.of("message", "Webhook processed successfully"));
            }
        } catch (Exception e) {
            System.err.println("Error parsing PayMongo webhook payload: " + e.getMessage());
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid webhook payload"));
    }
}
