
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Compensation;
import com.smarthr.backend.mapper.CompensationMapper;
import com.smarthr.backend.repository.CompensationRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.dto.CompensationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Tag(name = "Compensations", description = "Gestión de compensaciones salariales")
@RestController
@RequestMapping("/api/compensations")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationRepository repo;
    private final EmployeeRepository employeeRepo;
    private final CompensationMapper mapper;

    @Operation(summary = "Lista compensaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CompensationDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<CompensationDto>> list() {
        var list = repo.findAll().stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtiene compensación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = CompensationDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompensationDto> get(@Parameter(description = "ID de la compensación") @PathVariable Long id) {
        return repo.findById(id).map(mapper::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea compensación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(schema = @Schema(implementation = CompensationDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (duplicado por fecha)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CompensationDto> create(@Valid @RequestBody CompensationDto dto) {
        if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
            return ResponseEntity.badRequest().build();
        }

        Compensation entity = mapper.toEntity(dto);

        // Validar importes
        if (entity.getBaseSalary() == null || isNegative(entity.getBaseSalary())) {
            return ResponseEntity.badRequest().build();
        }
        if (entity.getBonus() != null && isNegative(entity.getBonus())) {
            return ResponseEntity.badRequest().build();
        }
        if (entity.getEffectiveFrom() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Opcional: evitar duplicados por (employee, effectiveFrom)
        // if (repo.existsByEmployeeIdAndEffectiveFrom(dto.getEmployeeId(), entity.getEffectiveFrom())) {
        //   return ResponseEntity.status(409).build();
        // }

        Compensation saved = repo.save(entity);
        return ResponseEntity.created(URI.create("/api/compensations/" + saved.getId()))
                .body(mapper.toDto(saved));
    }

    @Operation(summary = "Actualiza compensación (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = CompensationDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "ID de la compensación") @PathVariable Long id,
            @Valid @RequestBody CompensationDto dto) {

        return repo.findById(id).map(existing -> {
            if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
                return ResponseEntity.badRequest().build();
            }

            Compensation updated = mapper.toEntity(dto);
            updated.setId(id);

            if (updated.getBaseSalary() == null || isNegative(updated.getBaseSalary())) {
                return ResponseEntity.badRequest().build();
            }
            if (updated.getBonus() != null && isNegative(updated.getBonus())) {
                return ResponseEntity.badRequest().build();
            }
            if (updated.getEffectiveFrom() == null) {
                return ResponseEntity.badRequest().build();
            }

            Compensation saved = repo.save(updated);
            return ResponseEntity.ok(mapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina compensación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID de la compensación") @PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isNegative(BigDecimal value) {
        return value.signum() < 0;
    }
}
