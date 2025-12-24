
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.PerformanceReviewService;
import com.smarthr.backend.web.dto.PerformanceReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Performance Reviews", description = "Gestión de evaluaciones de desempeño")
@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewService service;

    @Operation(summary = "Lista evaluaciones (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PerformanceReviewDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<PerformanceReviewDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene evaluación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = PerformanceReviewDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> get(
            @Parameter(description = "ID de la evaluación") @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea evaluación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(schema = @Schema(implementation = PerformanceReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (duplicado por fecha)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> create(@Valid @RequestBody PerformanceReviewDto dto) {
        PerformanceReviewDto created = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/performance-reviews/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "Actualiza evaluación (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = PerformanceReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (duplicado por fecha)", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> update(
            @Parameter(description = "ID de la evaluación") @PathVariable Long id,
            @Valid @RequestBody PerformanceReviewDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina evaluación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la evaluación") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
