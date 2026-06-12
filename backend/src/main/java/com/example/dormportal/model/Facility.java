package com.example.dormportal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "facilities")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String status = "OPERATIONAL"; // OPERATIONAL, UNDER_REPAIR, OUT_OF_ORDER

    private String remarks;

    public Facility() {}

    public Facility(Long id, String name, String status, String remarks) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.remarks = remarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
