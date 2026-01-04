
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


@Tag(name = "Employees", description = "Gestión de empleados con filtros y paginación")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @Operation(summary = "Lista empleados", description = "Filtra por nombre, rol y ubicación. Resultados paginados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(
            @Parameter(description = "Filtro por nombre") @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por rol") @RequestParam(required = false) String role,
            @Parameter(description = "Filtro por ubicación") @RequestParam(required = false) String location,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(name, role, location, pageable));
    }

    @Operation(summary = "Obtiene un empleado por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empleado encontrado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea un empleado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        EmployeeDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza completamente un empleado (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Actualiza parcialmente un empleado (PATCH)")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> patch(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @Operation(summary = "Elimina un empleado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


