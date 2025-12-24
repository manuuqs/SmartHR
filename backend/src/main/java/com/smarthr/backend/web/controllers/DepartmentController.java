
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Departments", description = "Gestión de departamentos")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository repo;

    public DepartmentController(DepartmentRepository repo) { this.repo = repo; }

    @Operation(summary = "Lista departamentos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Department.class))))
    })
    @GetMapping
    public List<Department> list(){
        return repo.findAll();
    }

    @Operation(summary = "Obtiene un departamento por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado",
                    content = @Content(schema = @Schema(implementation = Department.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Department> get(
            @Parameter(description = "ID del departamento") @PathVariable Long id){
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un departamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado",
                    content = @Content(schema = @Schema(implementation = Department.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (nombre único)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody Department d){
        Department saved = repo.save(d);
        return ResponseEntity.created(URI.create("/api/departments/"+saved.getId())).body(saved);
    }

    @Operation(summary = "Actualiza un departamento (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado",
                    content = @Content(schema = @Schema(implementation = Department.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Department> update(
            @Parameter(description = "ID del departamento") @PathVariable Long id,
            @Valid @RequestBody Department d){
        return repo.findById(id).map(existing -> {
            existing.setName(d.getName());
            existing.setDescription(d.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina un departamento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del departamento") @PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
