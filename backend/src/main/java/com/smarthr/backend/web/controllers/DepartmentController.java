package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository repo;
    public DepartmentController(DepartmentRepository repo){ this.repo = repo; }

    @GetMapping
    public List<Department> list(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Department> get(@PathVariable Long id){
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody Department d){
        Department saved = repo.save(d);
        return ResponseEntity.created(URI.create("/api/departments/"+saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Long id, @Valid @RequestBody Department d){
        return repo.findById(id).map(existing -> {
            existing.setName(d.getName());
            existing.setDescription(d.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
