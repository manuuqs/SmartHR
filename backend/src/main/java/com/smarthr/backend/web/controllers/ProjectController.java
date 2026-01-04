package com.smarthr.backend.web.controllers;


import com.smarthr.backend.domain.Project;
import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.ProjectRepository;
import com.smarthr.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Projects", description = "Gestión de proyectos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectRepository repo;
    private final UserRepository userRepository;

    @Operation(summary = "Lista proyectos")
    @GetMapping
    public ResponseEntity<Page<Project>> list(@PageableDefault(size=20) Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(repo.findAll(pageable));
    }

    @Operation(summary = "Obtiene proyecto por id")
    @ApiResponses({ @ApiResponse(responseCode="200"), @ApiResponse(responseCode="404") })
    @GetMapping("/{id}") public ResponseEntity<Project> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea proyecto")
    @ApiResponses({ @ApiResponse(responseCode="201"), @ApiResponse(responseCode="400"), @ApiResponse(responseCode="409") })
    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody Project p) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        Project saved = repo.save(p);
        return ResponseEntity.created(URI.create("/api/projects/"+saved.getId())).body(saved);
    }

    @Operation(summary = "Actualiza proyecto (PUT)")
    @ApiResponses({ @ApiResponse(responseCode="200"), @ApiResponse(responseCode="404") })
    @PutMapping("/{id}") public ResponseEntity<Project> update(@PathVariable Long id, @Valid @RequestBody Project p) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return repo.findById(id).map(existing -> {
            existing.setCode(p.getCode());
            existing.setName(p.getName());
            existing.setClient(p.getClient());
            existing.setStartDate(p.getStartDate());
            existing.setEndDate(p.getEndDate());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina proyecto")
    @ApiResponses({ @ApiResponse(responseCode="204"), @ApiResponse(responseCode="404") })
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
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

