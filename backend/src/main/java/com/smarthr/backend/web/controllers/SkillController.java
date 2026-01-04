package com.smarthr.backend.web.controllers;


import com.smarthr.backend.domain.Skill;
import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.SkillRepository;
import com.smarthr.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Skills", description = "Gestión de skills")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository repo;
    private final UserRepository userRepository;

    @Operation(summary = "Lista skills")
    @GetMapping
    public List<Skill> list(){ return repo.findAll(); }

    @Operation(summary = "Obtiene skill por id")
    @GetMapping("/{id}") public ResponseEntity<Skill> get(@PathVariable Long id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea skill")
    @PostMapping
    public ResponseEntity<Skill> create(@Valid @RequestBody Skill s){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        Skill saved = repo.save(s);
        return ResponseEntity.created(URI.create("/api/skills/"+saved.getId())).body(saved);
    }

    @Operation(summary = "Actualiza skill (PUT)")
    @PutMapping("/{id}") public ResponseEntity<Skill> update(@PathVariable Long id, @Valid @RequestBody Skill s){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return repo.findById(id).map(existing -> {
            existing.setName(s.getName()); existing.setDescription(s.getDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Elimina skill")
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){

        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

