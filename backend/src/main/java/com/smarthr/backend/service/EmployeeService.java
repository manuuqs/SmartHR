
package com.smarthr.backend.service;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.ResourceNotFoundException;
import com.smarthr.backend.web.dto.EmployeeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para Employee.
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Page<EmployeeDto> list(String name, String role, String location, Pageable pageable) {
        Page<Employee> page = repository
                .findByNameContainingIgnoreCaseAndRoleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                        name == null ? "" : name,
                        role == null ? "" : role,
                        location == null ? "" : location,
                        pageable
                );
        return page.map(this::toDto);
    }

    public EmployeeDto get(Long id) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return toDto(e);
    }

    public EmployeeDto create(EmployeeDto dto) {
        Employee e = toEntity(dto);
        e.setId(null);
        e = repository.save(e);
        return toDto(e);
    }

    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        e.setName(dto.getName());
        e.setRole(dto.getRole());
        e.setLocation(dto.getLocation());
        e.setEmail(dto.getEmail());
        e.setHireDate(dto.getHireDate());
        e = repository.save(e);
        return toDto(e);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        repository.deleteById(id);
    }

    private EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setRole(e.getRole());
        dto.setLocation(e.getLocation());
        dto.setEmail(e.getEmail());
        dto.setHireDate(e.getHireDate());
        return dto;
    }

    private Employee toEntity(EmployeeDto dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setName(dto.getName());
        e.setRole(dto.getRole());
        e.setLocation(dto.getLocation());
        e.setEmail(dto.getEmail());
        e.setHireDate(dto.getHireDate());
        return e;
    }
}
