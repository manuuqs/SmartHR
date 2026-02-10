package com.smarthr.backend.repository;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteByEmployee(Employee employee);

    void deleteByEmployeeId(Long employeeId);
}

