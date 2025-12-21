package com.smarthr.backend.web.controllers;


import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.repository.JobPositionRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-positions")
public class JobPositionController {
    private final JobPositionRepository repo;
    public JobPositionController(JobPositionRepository repo){ this.repo = repo; }

    @GetMapping
    public List<JobPosition> list(){ return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<JobPosition> get(@PathVariable Long id){
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<JobPosition> create(@Valid @RequestBody JobPosition p){
        JobPosition saved = repo.save(p);
        return ResponseEntity.created(URI.create("/api/job-positions/"+saved.getId())).body(saved);
    }
    @PutMapping("/{id}") public ResponseEntity<JobPosition> update(@PathVariable Long id, @Valid @RequestBody JobPosition p){
        return repo.findById(id).map(existing -> {
            existing.setTitle(p.getTitle());
            existing.setDescription(p.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

