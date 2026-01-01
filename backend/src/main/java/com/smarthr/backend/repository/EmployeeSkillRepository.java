package com.smarthr.backend.repository;
import com.smarthr.backend.domain.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployee_Id(Long employeeId);
}
