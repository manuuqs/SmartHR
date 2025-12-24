
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.CompensationService;
import com.smarthr.backend.web.dto.CompensationDto;
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

@Tag(name = "Compensations", description = "Gestión de compensaciones salariales")
@RestController
@RequestMapping("/api/compensations")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService service;

    @Operation(summary = "Lista compensaciones (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CompensationDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<CompensationDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene compensación por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = CompensationDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompensationDto> get(
            @Parameter(description = "ID de la compensación") @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
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
        CompensationDto created = service.create(dto);
        return ResponseEntity
                .created(URI.create("/api/compensations/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "Actualiza compensación (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = CompensationDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (duplicado por fecha)", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompensationDto> update(
            @Parameter(description = "ID de la compensación") @PathVariable Long id,
            @Valid @RequestBody CompensationDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina compensación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la compensación") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
