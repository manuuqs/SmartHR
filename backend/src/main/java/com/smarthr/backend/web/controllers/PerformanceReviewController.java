
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.PerformanceReviewService;
import com.smarthr.backend.web.dto.PerformanceReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@Tag(name = "Performance Reviews", description = "Gestión de evaluaciones de desempeño")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewService service;

    @Operation(summary = "Lista evaluaciones (paginado)")
    @GetMapping
    public ResponseEntity<Page<PerformanceReviewDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene evaluación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea evaluación")
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> create(@Valid @RequestBody PerformanceReviewDto dto) {
        PerformanceReviewDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/performance-reviews/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza evaluación (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> update(@PathVariable Long id, @Valid @RequestBody PerformanceReviewDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina evaluación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


