package com.smarthr.backend.web.controllers;


import com.smarthr.backend.domain.EmployeeSkill;
import com.smarthr.backend.mapper.EmployeeSkillMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.EmployeeSkillRepository;
import com.smarthr.backend.repository.SkillRepository;
import com.smarthr.backend.web.dto.EmployeeSkillDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Employee Skills", description = "Skills por empleado")
@RestController
@RequestMapping("/api/employees/{employeeId}/skills")
@RequiredArgsConstructor
public class EmployeeSkillController {
    private final EmployeeSkillRepository repo;
    private final EmployeeRepository employeeRepo;
    private final SkillRepository skillRepo;
    private final EmployeeSkillMapper mapper;

    @Operation(summary="Lista skills de un empleado")
    @GetMapping
    public ResponseEntity<List<EmployeeSkillDto>> list(@PathVariable Long employeeId){
        if (!employeeRepo.existsById(employeeId)) return ResponseEntity.notFound().build();
        var list = repo.findAll().stream() // ideal: repo.findByEmployeeId(employeeId)
                .filter(es -> es.getEmployee().getId().equals(employeeId))
                .map(mapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary="Añade/actualiza nivel de una skill para un empleado")
    @PostMapping
    public ResponseEntity<EmployeeSkillDto> upsert(
            @PathVariable Long employeeId, @Valid @RequestBody EmployeeSkillDto dto){
        var emp = employeeRepo.findById(employeeId).orElse(null);
        var skl = skillRepo.findById(dto.getSkillId()).orElse(null);
        if (emp == null || skl == null) return ResponseEntity.badRequest().build();

        EmployeeSkill es = mapper.toEntity(dto);
        es.setEmployee(emp); es.setSkill(skl);
        EmployeeSkill saved = repo.save(es);
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @Operation(summary="Elimina una skill del empleado")
    @DeleteMapping("/{employeeSkillId}")
    public ResponseEntity<Void> delete(@PathVariable Long employeeId, @PathVariable Long employeeSkillId){
        if (!employeeRepo.existsById(employeeId)) return ResponseEntity.notFound().build();
        if (!repo.existsById(employeeSkillId)) return ResponseEntity.notFound().build();
        repo.deleteById(employeeSkillId);
        return ResponseEntity.noContent().build();
    }
}
