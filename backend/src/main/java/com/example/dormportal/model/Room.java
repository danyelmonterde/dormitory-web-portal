package com.example.dormportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomNumber;

    private Integer capacity;

    private Double monthlyRent;

    private String wifiPassword;

    public Room() {}

    public Room(Long id, String roomNumber, Integer capacity, Double monthlyRent, String wifiPassword) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.monthlyRent = monthlyRent;
        this.wifiPassword = wifiPassword;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Double getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(Double monthlyRent) { this.monthlyRent = monthlyRent; }

    public String getWifiPassword() { return wifiPassword; }
    public void setWifiPassword(String wifiPassword) { this.wifiPassword = wifiPassword; }
}
