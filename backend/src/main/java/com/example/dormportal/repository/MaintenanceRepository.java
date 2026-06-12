package com.example.dormportal.repository;

import com.example.dormportal.model.MaintenanceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceReport, Long> {
    List<MaintenanceReport> findByReporterId(Long reporterId);
    List<MaintenanceReport> findByRoomId(Long roomId);
}
