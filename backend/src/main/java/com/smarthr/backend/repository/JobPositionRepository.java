package com.smarthr.backend.repository;
import com.smarthr.backend.domain.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    Optional<JobPosition> findByTitle(String title);
}
