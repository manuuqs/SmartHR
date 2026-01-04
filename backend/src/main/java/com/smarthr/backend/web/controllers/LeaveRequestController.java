
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.LeaveRequestService;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@Tag(name = "Leave Requests", description = "Gestión de solicitudes de ausencia")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService service;

    @Operation(summary = "Crea solicitud de ausencia")
    @PostMapping
    public ResponseEntity<LeaveRequestDto> create(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequestDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/leave-requests/" + created.getId())).body(created);
    }

    @Operation(summary = "Aprueba o rechaza una solicitud")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequestDto> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }
}

