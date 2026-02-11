
package com.smarthr.backend.service;

import com.smarthr.backend.domain.*;
import com.smarthr.backend.repository.*;
import com.smarthr.backend.web.dto.*;
import com.smarthr.backend.web.mapper.AssignmentMapper;
import com.smarthr.backend.web.mapper.EmployeeMapper;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.mapper.EmployeeSkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Servicio de negocio para Employee.
 */


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    // ======= DEPENDENCIAS PARA EL "FULL" =======
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeSkillMapper employeeSkillMapper;

    private final ContractService contractService;
    private final CompensationService compensationService;
    private final EmployeeRagDtoService employeeRagDtoService;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;

    private final PerformanceReviewService performanceReviewService;
    private final LeaveRequestService leaveRequestService;
    private final UserRepository userRepository;

    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final ContractRepository contractRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final CompensationRepository compensationRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    public Employee createCompleteEmployee(NewEmployeeCompleteDto dto) {

        // 1. Crear Employee
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setLocation(dto.getLocation().toLowerCase());
        employee.setEmail(dto.getEmail());
        employee.setHireDate(dto.getHireDate());

        // Department
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
        employee.setDepartment(dept);

        // JobPosition
        JobPosition jobPos = jobPositionRepository.findByTitle(dto.getJobPositionTitle())
                .orElseGet(() -> {
                    JobPosition jp = new JobPosition();
                    jp.setTitle(dto.getJobPositionTitle());
                    return jobPositionRepository.save(jp);
                });
        employee.setJobPosition(jobPos);

        employee = repository.save(employee);

        // 2. Crear User
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.getRoles().add(dto.getRole());
        user.setEmployee(employee);
        userRepository.save(user);

        // 3. Crear Contract
        Contract contract = new Contract();
        contract.setEmployee(employee);
        contract.setType(Contract.ContractType.valueOf(dto.getContractType().name()));
        contract.setStartDate(dto.getContractStartDate());
        contract.setEndDate(dto.getContractEndDate());
        contract.setWeeklyHours(dto.getWeeklyHours());
        contractRepository.save(contract);

        // 4. Crear Assignment (si hay proyecto)
        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            JobPosition assignmentJobPos = jobPositionRepository.findByTitle(
                    dto.getAssignmentJobPosition() != null ?
                            dto.getAssignmentJobPosition() : dto.getJobPositionTitle()
            ).orElseGet(() -> {
                JobPosition jp = new JobPosition();
                jp.setTitle(dto.getAssignmentJobPosition() != null ?
                        dto.getAssignmentJobPosition() : dto.getJobPositionTitle());
                return jobPositionRepository.save(jp);
            });

            Assignment assignment = new Assignment();
            assignment.setEmployee(employee);
            assignment.setProject(project);
            assignment.setJobPosition(assignmentJobPos);  // ← Ahora SÍ es JobPosition
            assignment.setStartDate(dto.getContractStartDate());
            assignment.setEndDate(dto.getContractEndDate());
            assignmentRepository.save(assignment);
        }


        // 5. Crear EmployeeSkills
        if (dto.getSkillIds() != null && !dto.getSkillIds().isEmpty()) {
            for (Long skillId : dto.getSkillIds()) {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new RuntimeException("Skill no encontrada"));

                EmployeeSkill empSkill = new EmployeeSkill();
                empSkill.setEmployee(employee);
                empSkill.setSkill(skill);
                empSkill.setLevel(3); // Nivel por defecto
                employeeSkillRepository.save(empSkill);
            }
        }

        log.info(" LLAMADA HTTP AL ASSISTANT ");
        EmployeeCompleteDto ragEmployee = employeeRagDtoService.buildEmployeeRag(employee.getId());
        // --- LLAMADA HTTP AL ASSISTANT ---
        try {
            RestTemplate restTemplate = new RestTemplate();
            String assistantUrl = "http://assistant:9090/internal/rag/upsert-employee";

            ResponseEntity<String> response = restTemplate.postForEntity(
                    assistantUrl,
                    ragEmployee,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("No se pudo insertar el empleado en RAG. Respuesta: {}", response.getBody());
            }

        } catch (Exception e) {
            log.warn("Error llamando a assistant para insertar empleado en RAG: {}", e.getMessage());
        }


        return employee;
    }

    @Transactional
    public void deleteCompleteEmployee(Long employeeId) {

        Employee employee = repository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        // 1. USER (muy importante que vaya primero)
        userRepository.deleteByEmployeeId(employeeId);

        // 2. ASSIGNMENTS
        assignmentRepository.deleteByEmployeeId(employeeId);

        // 3. EMPLOYEE SKILLS
        employeeSkillRepository.deleteByEmployeeId(employeeId);

        // 4. CONTRACTS
        contractRepository.deleteByEmployeeId(employeeId);

        // 5. COMPENSATIONS
        compensationRepository.deleteByEmployeeId(employeeId);

        // 6. LEAVE REQUESTS
        leaveRequestRepository.deleteByEmployeeId(employeeId);

        // 7. PERFORMANCE REVIEWS
        performanceReviewRepository.deleteByEmployeeId(employeeId);

        // 8. EMPLOYEE
        repository.deleteById(employeeId);

        try {
            EmployeeRagDto dto = new EmployeeRagDto(
                    employee.getId(),
                    employee.getName(),
                    employee.getLocation(),
                    employee.getEmail(),
                    employee.getHireDate(),
                    employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                    employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                    employee.getJobPosition() != null ? employee.getJobPosition().getId() : null,
                    employee.getJobPosition() != null ? employee.getJobPosition().getTitle() : null
            );

            log.info("Borrando Employee en RAG: {}", employee.getName());

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(
                    "http://assistant:9090/internal/rag/delete-employee",
                    dto,
                    Void.class
            );

        } catch (Exception e) {
            log.warn("⚠️ No se pudo eliminar el employee en RAG: {}", e.getMessage());
        }

        log.info("Empleado eliminado correctamente");

    }


    @Transactional(readOnly = true)
    public List<EmployeeCompleteDto> getEmployeesCompleteRag() {

        List<Employee> employees = repository.findAll();

        return employees.stream()
                .map(this::buildEmployeeCompleteDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeCompleteDto getEmployeeCompleteRag(Long id) {

        Optional<Employee> employee = repository.findById(id);

        return employee.map(this::buildEmployeeCompleteDto)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    private EmployeeCompleteDto buildEmployeeCompleteDto(Employee employee) {

        Long employeeId = employee.getId();

        EmployeeDto empDto = mapper.toDto(employee);

        // Skills → nombres
        List<String> skills = employeeSkillRepository.findByEmployeeId(employeeId)
                .stream()
                .map(es -> es.getSkill().getName())
                .toList();

        // Proyectos
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

        String contractType = null;
        Integer weeklyHours = null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (contract != null) {
            contractType = contract.getType().name();
            weeklyHours = contract.getWeeklyHours();
            startDate = contract.getStartDate();
            endDate = contract.getEndDate();
        }

        // Salario (última compensación)
        Compensation compensation = compensationService
                .findLatestByEmployee(employeeId)
                .orElse(null);

        BigDecimal baseSalary = null;
        BigDecimal bonus = null;

        if (compensation != null) {
            baseSalary = compensation.getBaseSalary();
            bonus = compensation.getBonus();
        }

        // Ausencias
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
                contractType,
                weeklyHours,
                startDate,
                endDate,
                baseSalary,
                bonus,
                leaveRequests
        );

    }



}


