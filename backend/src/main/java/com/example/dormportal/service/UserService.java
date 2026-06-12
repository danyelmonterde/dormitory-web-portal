package com.example.dormportal.service;

import com.example.dormportal.model.Role;
import com.example.dormportal.model.Room;
import com.example.dormportal.model.User;
import com.example.dormportal.model.UserDTO;
import com.example.dormportal.repository.RoomRepository;
import com.example.dormportal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllDormers() {
        return userRepository.findByRole(Role.ROLE_DORMER).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        return toDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        return toDTO(user);
    }

    public UserDTO createUser(UserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        User user = new User();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(user, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to User", e);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new IllegalArgumentException("Password is required for user creation");
        }

        user.setRole(dto.getRole() != null ? Role.valueOf(dto.getRole()) : Role.ROLE_DORMER);
        user.setEnabled(dto.isEnabled());

        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + dto.getRoomId()));
            user.setRoom(room);
        }

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCustomCleaningFee(dto.getCustomCleaningFee());
        user.setEnabled(dto.isEnabled());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole()));
        }

        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + dto.getRoomId()));
            user.setRoom(room);
        } else {
            user.setRoom(null);
        }

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, user);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping User to DTO", e);
        }
        dto.setRole(user.getRole().name());
        if (user.getRoom() != null) {
            dto.setRoomId(user.getRoom().getId());
            dto.setRoomNumber(user.getRoom().getRoomNumber());
        }
        dto.setPassword(null); // Clear password for security
        return dto;
    }
}
