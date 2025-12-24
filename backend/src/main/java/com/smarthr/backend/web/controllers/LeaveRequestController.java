package com.smarthr.backend.web.controllers;


import com.smarthr.backend.domain.LeaveRequest;
import com.smarthr.backend.mapper.LeaveRequestMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.LeaveRequestRepository;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Leave Requests", description = "Gestión de solicitudes de ausencia")
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveRequestController {
    private final LeaveRequestRepository repo;
    private final EmployeeRepository employeeRepo;
    private final LeaveRequestMapper mapper;

    @Operation(summary="Crea solicitud de ausencia (PENDING por defecto)")
    @PostMapping public ResponseEntity<LeaveRequestDto> create(@Valid @RequestBody LeaveRequestDto dto){
        var emp = employeeRepo.findById(dto.getEmployeeId()).orElse(null);
        if (emp == null) return ResponseEntity.badRequest().build();
        LeaveRequest lr = mapper.toEntity(dto);
        lr.setEmployee(emp);
        if (lr.getEndDate().isBefore(lr.getStartDate())) return ResponseEntity.badRequest().build();
        // TODO: validar solapes en service
        LeaveRequest saved = repo.save(lr);
        return ResponseEntity.created(URI.create("/api/leaves/"+saved.getId())).body(mapper.toDto(saved));
    }

    @Operation(summary="Aprueba/Rechaza una solicitud")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequestDto> changeStatus(@PathVariable Long id, @RequestParam String status){
        return repo.findById(id).map(existing -> {
            existing.setStatus(LeaveRequest.LeaveStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(mapper.toDto(repo.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }
}
