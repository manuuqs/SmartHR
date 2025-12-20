
package com.smarthr.backend.repository;

import com.smarthr.backend.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA con búsqueda filtrada y paginada.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByNameContainingIgnoreCaseAndRoleContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String name, String role, String location, Pageable pageable
    );
}

