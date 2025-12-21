package com.smarthr.backend.repository;
import com.smarthr.backend.domain.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {}
