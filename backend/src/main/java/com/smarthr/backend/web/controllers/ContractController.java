
package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.ContractRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.service.ContractService;
import com.smarthr.backend.web.dto.ContractDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@Tag(name = "Contracts", description = "Gestión de contratos de empleados")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService service;
    private final UserRepository userRepository;
    private final ContractRepository repo;

    @Operation(summary = "Lista contratos (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractDto.class))))
    })
    @GetMapping
    public ResponseEntity<Page<ContractDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(summary = "Obtiene contrato por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado",
                    content = @Content(schema = @Schema(implementation = ContractDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContractDto> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar

        if (!user.getRoles().contains("ROLE_RRHH")) {
            var entity = repo.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
            if (!entity.getEmployee().getId().equals(user.getEmployee().getId())) {
                throw new AccessDeniedException("No tienes permiso para ver otros empleados");
            }
        }

        return ResponseEntity.ok(service.get(id));
    }

    @Operation(summary = "Crea contrato")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida"),
            @ApiResponse(responseCode = "409", description = "Conflicto (solapes o reglas de negocio)")
    })
    @PostMapping
    public ResponseEntity<ContractDto> create(@Valid @RequestBody ContractDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        ContractDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/contracts/" + created.getId())).body(created);
    }

    @Operation(summary = "Actualiza contrato (PUT)")
    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> update(@PathVariable Long id, @Valid @RequestBody ContractDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Elimina contrato")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Si no es RRHH y el ID no coincide con su empleado, denegar
        if (!user.getRoles().contains("ROLE_RRHH")) {
            throw new AccessDeniedException("No tienes permiso para ver otros empleados");
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

