
package com.smarthr.backend.service;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.mapper.EmployeeMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.ResourceNotFoundException;
import com.smarthr.backend.web.dto.EmployeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para Employee.
 */


@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public Page<EmployeeDto> list(String name, String role, String location, Pageable pageable) {
        Page<Employee> page = repository
                .findByNameContainingIgnoreCaseAndRoleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                        name == null ? "" : name,
                        role == null ? "" : role,
                        location == null ? "" : location,
                        pageable
                );
        return page.map(mapper::toDto);
    }

    public EmployeeDto get(Long id) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return mapper.toDto(e);
    }

    public EmployeeDto create(EmployeeDto dto) {
        Employee e = mapper.toEntity(dto);
        e.setId(null);
        e = repository.save(e);
        return mapper.toDto(e);
    }

    // ====== Actualización parcial ======
    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee current = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        // IGNORA nulls (no pisa lo existente)
        mapper.updateEntityFromDto(dto, current);

        Employee saved = repository.save(current);
        return mapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        repository.deleteById(id);
    }
}


