
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.User;
import com.smarthr.backend.web.mapper.AssignmentMapper;
import com.smarthr.backend.domain.Assignment;
import com.smarthr.backend.repository.AssignmentRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.ProjectRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.web.dto.AssignmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Assignments", description = "Asignaciones de empleados a proyectos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentRepository repo;
    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepo;
    private final AssignmentMapper mapper;

    @Operation(summary = "Crea una asignación empleado-proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(schema = @Schema(implementation = AssignmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (unicidad)", content = @Content)
    })

    @PostMapping
    public ResponseEntity<AssignmentDto> create(@Valid @RequestBody AssignmentDto dto) {
        if (dto.getEmployeeId() == null || dto.getProject() == null || dto.getProject().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        var emp = employeeRepo.findById(dto.getEmployeeId()).orElse(null);
        var prj = projectRepo.findById(dto.getProject().getId()).orElse(null);
        if (emp == null || prj == null) return ResponseEntity.badRequest().build();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }

        Assignment entity = mapper.toEntity(dto);
        entity.setEmployee(emp);
        entity.setProject(prj);

        if (entity.getStartDate() != null && entity.getEndDate() != null
                && entity.getEndDate().isBefore(entity.getStartDate())) {
            return ResponseEntity.badRequest().build();
        }

        Assignment saved = repo.save(entity);
        AssignmentDto out = mapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/assignments/" + saved.getId())).body(out);
    }


    @Operation(summary = "Lista todas las asignaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AssignmentDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<AssignmentDto>> list() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        var list = repo.findAll().stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtiene una asignación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = AssignmentDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDto> get(
            @Parameter(description = "ID de la asignación") @PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar

        if (!user.getRoles().contains("ROLE_RRHH")) {
            var entity = repo.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
            if (!entity.getEmployee().getId().equals(user.getEmployee().getId())) {
                throw new AccessDeniedException("No tienes permiso para ver otros empleados");
            }
        }

        return repo.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualiza completamente una asignación (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = AssignmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "ID de la asignación") @PathVariable Long id,
            @Valid @RequestBody AssignmentDto dto) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validación de permisos
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para modificar asignaciones");
        }

        return repo.findById(id).map(existing -> {
            // Validar datos del DTO
            if (dto.getEmployeeId() == null || dto.getProject() == null || dto.getProject().getId() == null) {
                return ResponseEntity.badRequest().build();
            }

            var emp = employeeRepo.findById(dto.getEmployeeId()).orElse(null);
            var prj = projectRepo.findById(dto.getProject().getId()).orElse(null);
            if (emp == null || prj == null) return ResponseEntity.badRequest().build();

            // Mapear y actualizar
            Assignment updated = mapper.toEntity(dto);
            updated.setId(id);
            updated.setEmployee(emp);
            updated.setProject(prj);

            // Validar fechas
            if (updated.getStartDate() != null && updated.getEndDate() != null
                    && updated.getEndDate().isBefore(updated.getStartDate())) {
                return ResponseEntity.badRequest().build();
            }

            Assignment saved = repo.save(updated);
            return ResponseEntity.ok(mapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }


    @Operation(summary = "Elimina una asignación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la asignación") @PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
