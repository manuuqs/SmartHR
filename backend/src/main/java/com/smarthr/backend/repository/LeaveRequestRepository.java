package com.smarthr.backend.repository;
import com.smarthr.backend.domain.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {}
