
package com.smarthr.backend.service;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.AssignmentRepository;
import com.smarthr.backend.repository.EmployeeSkillRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.web.mapper.AssignmentMapper;
import com.smarthr.backend.web.mapper.EmployeeMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.dto.EmployeeDto;
import com.smarthr.backend.web.mapper.EmployeeSkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de negocio para Employee.
 */


@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    // ======= DEPENDENCIAS PARA EL "FULL" =======
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeSkillMapper employeeSkillMapper;

    private final ContractService contractService;
    private final CompensationService compensationService;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;

    private final PerformanceReviewService performanceReviewService;
    private final LeaveRequestService leaveRequestService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> list(String name, String jobPosition, String location, Pageable pageable) {
        Page<Employee> page = repository
                .findByNameContainingIgnoreCaseAndJobPosition_TitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                        name == null ? "" : name,
                        jobPosition == null ? "" : jobPosition,
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

    @Transactional(readOnly = true)
    public Map<String, Object> getFullEmployeeByUsername(String usernameParam, User currentUser) {

        // Buscar el usuario por username
        User user = userRepository.findByUsername(usernameParam)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usernameParam));

        // Obtener el employee asociado
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new ResourceNotFoundException("No existe un empleado asociado al usuario: " + usernameParam);
        }

        // 🔐 Control de permisos
        if (!currentUser.getRoles().contains("ROLE_RRHH")
                && !employee.getId().equals(currentUser.getEmployee().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }

        Long employeeId = employee.getId();
        Map<String, Object> response = new HashMap<>();
        response.put("employee", mapper.toDto(employee));

        // Skills
        response.put(
                "skills",
                employeeSkillRepository.findByEmployeeId(employeeId)
                        .stream()
                        .map(employeeSkillMapper::toDto)
                        .toList()
        );

        // Contracts
        response.put("contracts", contractService.listByEmployee(employeeId));

        // Compensations
        response.put("compensations", compensationService.listByEmployee(employeeId));

        // Assignments
        response.put(
                "assignments",
                assignmentRepository.findByEmployeeId(employeeId)
                        .stream()
                        .map(assignmentMapper::toDto)
                        .toList()
        );

        // Performance Reviews
        response.put(
                "performanceReviews",
                performanceReviewService.listByEmployee(employeeId)
        );

        // Leave Requests
        response.put(
                "leaveRequests",
                leaveRequestService.listByEmployee(employeeId)
        );

        return response;
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


