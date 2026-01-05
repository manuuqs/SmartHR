package com.smarthr.backend.service;


import com.smarthr.backend.domain.EmployeeSkill;
import com.smarthr.backend.mapper.EmployeeSkillMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.EmployeeSkillRepository;
import com.smarthr.backend.repository.SkillRepository;
import com.smarthr.backend.web.ResourceNotFoundException;
import com.smarthr.backend.web.dto.EmployeeSkillDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeSkillService {
    private final EmployeeSkillRepository repo;
    private final EmployeeRepository employeeRepo;
    private final SkillRepository skillRepo;
    private final EmployeeSkillMapper mapper;

    @Transactional(readOnly = true)
    public List<EmployeeSkillDto> listByEmployee(Long employeeId) {
        if (!employeeRepo.existsById(employeeId)) throw new ResourceNotFoundException("Empleado no existe");
        return repo.findByEmployeeId(employeeId).stream().map(mapper::toDto).toList();
    }

    public EmployeeSkillDto upsertSkill(Long employeeId, EmployeeSkillDto dto) {
        var emp = employeeRepo.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Empleado no existe"));
        var skl = skillRepo.findById(dto.getSkillId()).orElseThrow(() -> new ResourceNotFoundException("Skill no existe"));
        EmployeeSkill entity = mapper.toEntity(dto);
        entity.setEmployee(emp);
        entity.setSkill(skl);
        EmployeeSkill saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    public void deleteSkill(Long employeeId, Long employeeSkillId) {
        if (!employeeRepo.existsById(employeeId)) throw new ResourceNotFoundException("Empleado no existe");
        if (!repo.existsById(employeeSkillId)) throw new ResourceNotFoundException("Skill no existe");
        repo.deleteById(employeeSkillId);
    }
}

