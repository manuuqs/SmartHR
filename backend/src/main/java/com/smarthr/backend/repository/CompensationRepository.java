package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Compensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;


public interface CompensationRepository extends JpaRepository<Compensation, Long> {
    boolean existsByEmployee_IdAndEffectiveFrom(Long employeeId, java.time.LocalDate effectiveFrom);

    Collection<Compensation> findByEmployeeId(Long employeeId);
}
