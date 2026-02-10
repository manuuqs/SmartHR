package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Contract;
import com.smarthr.backend.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    java.util.List<Contract> findByEmployeeId(Long employeeId);

    Optional<Contract> findFirstByEmployeeIdOrderByStartDateDesc(Long empId);

    void deleteByEmployee(Employee employee);

    void deleteByEmployeeId(Long employeeId);
}

