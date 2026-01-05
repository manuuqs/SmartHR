
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.User;
import com.smarthr.backend.mapper.AssignmentMapper;
import com.smarthr.backend.mapper.EmployeeMapper;
import com.smarthr.backend.mapper.EmployeeSkillMapper;
import com.smarthr.backend.repository.AssignmentRepository;
import com.smarthr.backend.repository.EmployeeSkillRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.service.*;
import com.smarthr.backend.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Tag(name = "Employees", description = "Gestión de empleados con filtros y paginación")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;
    private final ContractService contractService;
    private final CompensationService compensationService;
    private final EmployeeSkillMapper employeeSkillMapper;
    private final AssignmentMapper assignmentMapper;
    private final PerformanceReviewService performanceReviewService;
    private final LeaveRequestService leaveRequestService;

    private final UserRepository userRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeMapper mapper;

    @Operation(summary = "Lista empleados", description = "Filtra por nombre, rol y ubicación. Resultados paginados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(
            @Parameter(description = "Filtro por nombre") @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por rol") @RequestParam(required = false) String role,
            @Parameter(description = "Filtro por ubicación") @RequestParam(required = false) String location,
            @PageableDefault(size = 20) Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH denegar acceso
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.list(name, role, location, pageable));
    }

    @Operation(summary = "Obtiene un empleado por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleado encontrado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH") && !id.equals(user.getEmployee().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }

        return ResponseEntity.ok(service.get(id));
    }


    @Operation(summary = "Crea un empleado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        EmployeeDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza completamente un empleado (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Actualiza parcialmente un empleado (PATCH)")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> patch(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @Operation(summary = "Elimina un empleado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/full")
    public ResponseEntity<Map<String, Object>> getMyFullData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("username: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getEmployee() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long employeeId = user.getEmployee().getId();
        System.out.println("employeeId: " + employeeId);

        Map<String, Object> response = new HashMap<>();
        response.put("employee", mapper.toDto(user.getEmployee()));

        // Skills
        List<EmployeeSkillDto> skills = employeeSkillRepository.findByEmployeeId(employeeId)
                .stream().map(employeeSkillMapper::toDto).toList();
        response.put("skills", skills);

        // Contracts
        List<ContractDto> contracts = contractService.listByEmployee(employeeId);
        response.put("contracts", contracts);

        // Compensations
        List<CompensationDto> compensations = compensationService.listByEmployee(employeeId);
        response.put("compensations", compensations);

        // Assignments (proyectos)
        List<AssignmentDto> assignments = assignmentRepository.findByEmployeeId(employeeId)
                .stream().map(assignmentMapper::toDto).toList();
        response.put("assignments", assignments);

        // Performance Reviews
        List<PerformanceReviewDto> reviews = performanceReviewService.listByEmployee(employeeId);
        response.put("performanceReviews", reviews);

        // Leave Requests
        List<LeaveRequestDto> leaves = leaveRequestService.listByEmployee(employeeId);
        response.put("leaveRequests", leaves);

        return ResponseEntity.ok(response);
    }

}


