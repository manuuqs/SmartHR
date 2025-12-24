
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

    @Transactional(readOnly = true)
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

    @Transactional
    public EmployeeDto create(EmployeeDto dto) {
        Employee e = mapper.toEntity(dto);
        e.setId(null);
        e = repository.save(e);
        return mapper.toDto(e);
    }


    @Transactional
    public EmployeeDto update(Long id, EmployeeDto dto) {
        Employee current = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        // Reemplazo completo: crea desde DTO y sustituye
        Employee updated = mapper.toEntity(dto);
        updated.setId(id);
        // (Opcional) normaliza relaciones: department/jobPosition por id
        return mapper.toDto(repository.save(updated));
    }

    @Transactional
    public EmployeeDto patch(Long id, EmployeeDto dto) {
        Employee current = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        // Parcial: ignora nulos (no pisa campos no enviados)
        mapper.updateEntityFromDto(dto, current);
        return mapper.toDto(repository.save(current));
    }



    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        repository.deleteById(id);
    }
}


