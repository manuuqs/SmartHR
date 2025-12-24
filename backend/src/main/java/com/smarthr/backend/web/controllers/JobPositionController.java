
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.repository.JobPositionRepository;
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

@Tag(name = "Job Positions", description = "Gestión de posiciones de trabajo")
@RestController
@RequestMapping("/api/job-positions")
public class JobPositionController {

    private final JobPositionRepository repo;

    public JobPositionController(JobPositionRepository repo){ this.repo = repo; }

    @Operation(summary = "Lista posiciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = JobPosition.class))))
    })
    @GetMapping
    public List<JobPosition> list(){ return repo.findAll(); }

    @Operation(summary = "Obtiene una posición por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada",
                    content = @Content(schema = @Schema(implementation = JobPosition.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<JobPosition> get(
            @Parameter(description = "ID de la posición") @PathVariable Long id){
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea una posición")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creada",
                    content = @Content(schema = @Schema(implementation = JobPosition.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto (título único)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<JobPosition> create(@Valid @RequestBody JobPosition p){
        JobPosition saved = repo.save(p);
        return ResponseEntity.created(URI.create("/api/job-positions/"+saved.getId())).body(saved);
    }

    @Operation(summary = "Actualiza una posición (PUT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizada",
                    content = @Content(schema = @Schema(implementation = JobPosition.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<JobPosition> update(
            @Parameter(description = "ID de la posición") @PathVariable Long id,
            @Valid @RequestBody JobPosition p){
        return repo.findById(id).map(existing -> {
            existing.setTitle(p.getTitle());
            existing.setDescription(p.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina una posición")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la posición") @PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
