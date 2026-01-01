
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Departments", description = "Catálogo de departamentos")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository repo;

    @Operation(summary = "Lista departamentos")
    @GetMapping
    public ResponseEntity<List<Department>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @Operation(summary = "Obtiene un departamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Department> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un departamento")
    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody Department d) {
        Department saved = repo.save(d);
        return ResponseEntity.created(URI.create("/api/departments/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Actualiza un departamento")
    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @Valid @RequestBody Department d) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(d.getName());
                    existing.setDescription(d.getDescription());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina un departamento")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
