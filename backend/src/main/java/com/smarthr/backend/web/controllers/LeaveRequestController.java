
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.service.LeaveRequestService;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@Tag(name = "Leave Requests", description = "Gestión de solicitudes de ausencia")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService service;
    private final UserRepository userRepository;

    @Operation(summary = "Crea solicitud de ausencia")
    @PostMapping
    public ResponseEntity<LeaveRequestDto> create(@Valid @RequestBody LeaveRequestDto dto) {
        System.out.println("create: " + dto);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LeaveRequestDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/leave-requests/" + created.getId())).body(created);
    }

    @Operation(summary = "Aprueba o rechaza una solicitud")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequestDto> changeStatus(@PathVariable Long id, @RequestParam String status) {
        System.out.println("Cambio de estado solicitado: " + status);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.changeStatus(id, status));
    }

    @Operation(summary = "Lista todas las solicitudes de leave pendientes")
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDto>> getPendingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver solicitudes pendientes");
        }

        List<LeaveRequestDto> pendingRequests = service.getPendingRequests();
        return ResponseEntity.ok(pendingRequests);
    }
}

