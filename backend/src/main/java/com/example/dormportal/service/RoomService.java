package com.example.dormportal.service;

import com.example.dormportal.model.Room;
import com.example.dormportal.model.RoomDTO;
import com.example.dormportal.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));
        return toDTO(room);
    }

    public RoomDTO createRoom(RoomDTO dto) {
        if (roomRepository.findByRoomNumber(dto.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Room number already exists: " + dto.getRoomNumber());
        }

        Room room = new Room();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(room, dto);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping DTO to Room", e);
        }

        Room saved = roomRepository.save(room);
        return toDTO(saved);
    }

    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + id));

        room.setCapacity(dto.getCapacity());
        room.setMonthlyRent(dto.getMonthlyRent());
        room.setWifiPassword(dto.getWifiPassword());
        room.setRoomNumber(dto.getRoomNumber());

        Room saved = roomRepository.save(room);
        return toDTO(saved);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public RoomDTO toDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        try {
            org.apache.commons.beanutils.BeanUtils.copyProperties(dto, room);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping Room to DTO", e);
        }
        return dto;
    }
}
