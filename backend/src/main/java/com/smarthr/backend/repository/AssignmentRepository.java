package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Assignment;
import com.smarthr.backend.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByEmployeeId(Long employeeId);

    void deleteByEmployee(Employee employee);

    void deleteByEmployeeId(Long employeeId);
}
