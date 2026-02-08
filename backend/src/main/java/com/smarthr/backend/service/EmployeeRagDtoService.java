package com.smarthr.backend.service;

import com.smarthr.backend.domain.Compensation;
import com.smarthr.backend.domain.Contract;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.repository.AssignmentRepository;
import com.smarthr.backend.repository.ContractRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.EmployeeSkillRepository;
import com.smarthr.backend.web.dto.EmployeeCompleteDto;
import com.smarthr.backend.web.dto.EmployeeDto;
import com.smarthr.backend.web.dto.ProjectRagDto;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeRagDtoService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper mapper;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AssignmentRepository assignmentRepository;
    private final ContractRepository contractRepository;
    private final CompensationService compensationService;
    private final LeaveRequestService leaveRequestService;

    public EmployeeCompleteDto buildEmployeeRag(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        EmployeeDto empDto = mapper.toDto(employee);

        List<String> skills = employeeSkillRepository.findByEmployeeId(employeeId)
                .stream()
                .map(es -> es.getSkill().getName())
                .toList();

        List<ProjectRagDto> projects = assignmentRepository.findByEmployeeId(employeeId)
                .stream()
                .map(a -> a.getProject())
                .distinct()
                .map(p -> new ProjectRagDto(
                        p.getCode(),
                        p.getName(),
                        p.getClient(),
                        p.getUbication(),
                        p.getStartDate(),
                        p.getEndDate()
                ))
                .toList();

        Contract contract = contractRepository
                .findFirstByEmployeeIdOrderByStartDateDesc(employeeId)
                .orElse(null);

        Compensation compensation = compensationService
                .findLatestByEmployee(employeeId)
                .orElse(null);

        List<String> leaveRequests = leaveRequestService.listByEmployee(employeeId)
                .stream()
                .map(lr -> lr.getType() + " (" + lr.getStartDate() + " - " + lr.getEndDate() + ")")
                .toList();

        return new EmployeeCompleteDto(
                empDto.getId(),
                empDto.getName(),
                empDto.getEmail(),
                empDto.getLocation(),
                empDto.getHireDate(),
                empDto.getDepartmentName(),
                empDto.getJobPositionTitle(),
                skills,
                projects,
                contract != null ? contract.getType().name() : null,
                contract != null ? contract.getWeeklyHours() : null,
                contract != null ? contract.getStartDate() : null,
                contract != null ? contract.getEndDate() : null,
                compensation != null ? compensation.getBaseSalary() : null,
                compensation != null ? compensation.getBonus() : null,
                leaveRequests
        );
    }
}

