
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.LeaveRequestService;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Leave Requests", description = "Cambio de estado global")
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService service;

    @Operation(summary = "Aprueba o rechaza una solicitud")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveRequestDto> changeStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }
}
