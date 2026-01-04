
package com.smarthr.backend.web.controllers;

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
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@Tag(name = "Compensations", description = "Gestión de compensaciones salariales")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/compensations")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService service;

    @Operation(summary = "Lista compensaciones (paginado)")
    @GetMapping
    public ResponseEntity<Page<CompensationDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene compensación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CompensationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea compensación")
    @PostMapping
    public ResponseEntity<CompensationDto> create(@Valid @RequestBody CompensationDto dto) {
        CompensationDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/compensations/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza compensación (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<CompensationDto> update(@PathVariable Long id, @Valid @RequestBody CompensationDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina compensación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

