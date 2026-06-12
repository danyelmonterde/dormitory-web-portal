package com.example.dormportal.service;

import com.example.dormportal.model.Facility;
import com.example.dormportal.repository.FacilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public Facility getFacilityById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found with ID: " + id));
    }

    public Facility createFacility(Facility facility) {
        if (facilityRepository.findByName(facility.getName()).isPresent()) {
            throw new IllegalArgumentException("Facility name already exists: " + facility.getName());
        }
        return facilityRepository.save(facility);
    }

    public Facility updateFacility(Long id, Facility details) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found with ID: " + id));

        facility.setName(details.getName());
        facility.setStatus(details.getStatus());
        facility.setRemarks(details.getRemarks());

        return facilityRepository.save(facility);
    }

    public void deleteFacility(Long id) {
        facilityRepository.deleteById(id);
    }
}
