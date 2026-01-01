
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.EmployeeService;
import com.smarthr.backend.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@Tag(name = "Employees", description = "CRUD de empleados con filtros y paginación")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @Operation(
            summary = "Lista empleados",
            description = "Filtra por nombre, rol y ubicación. Resultados paginados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(
            @Parameter(description = "Filtro por nombre (contiene, ignore-case)") @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por rol (contiene, ignore-case)") @RequestParam(required = false) String role,
            @Parameter(description = "Filtro por ubicación (contiene, ignore-case)") @RequestParam(required = false) String location,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(name, role, location, pageable));
    }

    @Operation(summary = "Obtiene un empleado por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleado encontrado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@Parameter(description = "Identificador del empleado") @PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea un empleado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (unicidades)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        EmployeeDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza completamente un empleado (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@Parameter(description = "ID del empleado") @PathVariable Long id,
                                              @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Actualiza parcialmente un empleado (PATCH)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> patch(@Parameter(description = "ID del empleado") @PathVariable Long id,
                                             @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @Operation(summary = "Elimina un empleado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID del empleado") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
