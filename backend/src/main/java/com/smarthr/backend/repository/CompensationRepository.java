package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Compensation;
import com.smarthr.backend.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;


public interface CompensationRepository extends JpaRepository<Compensation, Long> {
    boolean existsByEmployee_IdAndEffectiveFrom(Long employeeId, java.time.LocalDate effectiveFrom);

    Collection<Compensation> findByEmployeeId(Long employeeId);

    Optional<Compensation> findFirstByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    void deleteByEmployee(Employee employee);

    void deleteByEmployeeId(Long employeeId);
}
