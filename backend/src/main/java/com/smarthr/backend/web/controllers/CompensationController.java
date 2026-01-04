
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.CompensationRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.service.CompensationService;
import com.smarthr.backend.web.dto.CompensationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
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


@Tag(name = "Compensations", description = "Gestión de compensaciones salariales")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/compensations")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService service;
    private final UserRepository userRepository;
    private final CompensationRepository repo;

    @Operation(summary = "Lista compensaciones (paginado)")
    @GetMapping
    public ResponseEntity<Page<CompensationDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene compensación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompensationDto> get(@PathVariable Long id) {
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

        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea compensación")
    @PostMapping
    public ResponseEntity<CompensationDto> create(@Valid @RequestBody CompensationDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        CompensationDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/compensations/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza compensación (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<CompensationDto> update(@PathVariable Long id, @Valid @RequestBody CompensationDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina compensación")
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
}

