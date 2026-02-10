package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    boolean existsByEmployee_IdAndReviewDate(Long employeeId, java.time.LocalDate reviewDate);
    List<PerformanceReview> findByEmployeeId(Long employeeId);

    void deleteByEmployee(Employee employee);

    void deleteByEmployeeId(Long employeeId);
}
