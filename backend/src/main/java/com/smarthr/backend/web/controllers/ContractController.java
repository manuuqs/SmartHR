
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Contract;
import com.smarthr.backend.mapper.ContractMapper;
import com.smarthr.backend.repository.ContractRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.dto.ContractDto;
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

@Tag(name = "Contracts", description = "Gestión de contratos de empleados")
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository repo;
    private final EmployeeRepository employeeRepo;
    private final ContractMapper mapper;

    @Operation(summary = "Lista contratos (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ContractDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<ContractDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(repo.findAll(pageable).map(mapper::toDto));
    }

    @Operation(summary = "Obtiene contrato por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado",
                    content = @Content(schema = @Schema(implementation = ContractDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContractDto> get(@Parameter(description = "ID del contrato") @PathVariable Long id) {
        return repo.findById(id).map(mapper::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea contrato")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = ContractDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (solapes o reglas de negocio)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ContractDto> create(@Valid @RequestBody ContractDto dto) {
        // Validar empleado
        if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
            return ResponseEntity.badRequest().build();
        }

        // MapStruct convertirá type (String) -> enum; si es inválido, valueOf lanza IllegalArgumentException
        Contract entity;
        try {
            entity = mapper.toEntity(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        // Reglas de fecha: startDate obligatorio; endDate >= startDate si viene; para tipos no permanentes, endDate recomendado
        if (entity.getStartDate() == null) return ResponseEntity.badRequest().build();
        if (entity.getEndDate() != null && entity.getEndDate().isBefore(entity.getStartDate())) {
            return ResponseEntity.badRequest().build();
        }

        // Validar horas semanales si vienen (1..60 de ejemplo)
        if (entity.getWeeklyHours() != null) {
            int h = entity.getWeeklyHours();
            if (h < 1 || h > 60) return ResponseEntity.badRequest().build();
        }

        Contract saved = repo.save(entity);
        return ResponseEntity.created(URI.create("/api/contracts/" + saved.getId()))
                .body(mapper.toDto(saved));
    }

    @Operation(summary = "Actualiza contrato (PUT - reemplazo completo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado",
                    content = @Content(schema = @Schema(implementation = ContractDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "ID del contrato") @PathVariable Long id,
            @Valid @RequestBody ContractDto dto) {

        return repo.findById(id).map(existing -> {
            // Validar empleado
            if (dto.getEmployeeId() == null || !employeeRepo.existsById(dto.getEmployeeId())) {
                return ResponseEntity.badRequest().build();
            }

            // toEntity + reemplazo completo
            Contract updated;
            try {
                updated = mapper.toEntity(dto);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
            updated.setId(id);

            // Validaciones de fecha y horas
            if (updated.getStartDate() == null) return ResponseEntity.badRequest().build();
            if (updated.getEndDate() != null && updated.getEndDate().isBefore(updated.getStartDate())) {
                return ResponseEntity.badRequest().build();
            }
            if (updated.getWeeklyHours() != null) {
                int h = updated.getWeeklyHours();
                if (h < 1 || h > 60) return ResponseEntity.badRequest().build();
            }

            Contract saved = repo.save(updated);
            return ResponseEntity.ok(mapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina contrato")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID del contrato") @PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
