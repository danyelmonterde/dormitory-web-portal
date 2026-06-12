package com.example.dormportal.repository;

import com.example.dormportal.model.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentReport, Long> {
    List<IncidentReport> findByReporterId(Long reporterId);
}
