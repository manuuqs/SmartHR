
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.PerformanceReview;
import com.smarthr.backend.mapper.PerformanceReviewMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.PerformanceReviewRepository;
import com.smarthr.backend.web.dto.PerformanceReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Performance Reviews", description = "Gestión de evaluaciones de desempeño")
@RestController
@RequestMapping("/api/performance-reviews")
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewRepository repo;
    private final EmployeeRepository employeeRepo;
    private final PerformanceReviewMapper mapper;

    @Operation(summary = "Lista evaluaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PerformanceReviewDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<PerformanceReviewDto>> list() {
        var list = repo.findAll().stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtiene evaluación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = PerformanceReviewDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> get(@Parameter(description = "ID de la evaluación") @PathVariable Long id) {
        return repo.findById(id).map(mapper::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
        if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
            return ResponseEntity.badRequest().build();
        }

        PerformanceReview entity;
        try {
            entity = mapper.toEntity(dto); // rating: String -> enum
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        // Validar fecha y comentario
        if (entity.getReviewDate() == null) return ResponseEntity.badRequest().build();
        if (entity.getReviewDate().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build(); // opcional: no permitir fecha futura
        }
        if (entity.getComments() != null && entity.getComments().length() > 1000) {
            return ResponseEntity.badRequest().build();
        }

        // Opcional: impedir duplicado (employeeId, reviewDate)
        // if (repo.existsByEmployeeIdAndReviewDate(dto.getEmployeeId(), entity.getReviewDate())) {
        //   return ResponseEntity.status(409).build();
        // }

        PerformanceReview saved = repo.save(entity);
        return ResponseEntity.created(URI.create("/api/performance-reviews/" + saved.getId()))
                .body(mapper.toDto(saved));
    }

    @Operation(summary = "Actualiza evaluación (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = PerformanceReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "ID de la evaluación") @PathVariable Long id,
            @Valid @RequestBody PerformanceReviewDto dto) {

        return repo.findById(id).map(existing -> {
            if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
                return ResponseEntity.badRequest().build();
            }

            PerformanceReview updated;
            try {
                updated = mapper.toEntity(dto);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
            updated.setId(id);

            if (updated.getReviewDate() == null || updated.getReviewDate().isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().build();
            }
            if (updated.getComments() != null && updated.getComments().length() > 1000) {
                return ResponseEntity.badRequest().build();
            }

            PerformanceReview saved = repo.save(updated);
            return ResponseEntity.ok(mapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina evaluación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID de la evaluación") @PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
