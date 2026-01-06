
package com.smarthr.backend.service;

import com.smarthr.backend.domain.Contract;
import com.smarthr.backend.web.mapper.ContractMapper;
import com.smarthr.backend.repository.ContractRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.exceptions.ConflictException;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.dto.ContractDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository repo;
    private final EmployeeRepository employeeRepo;
    private final ContractMapper mapper;

    @Transactional(readOnly = true)
    public Page<ContractDto> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public ContractDto get(Long id) {
        Contract c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract " + id + " no existe"));
        return mapper.toDto(c);
    }

    public ContractDto create(ContractDto dto) {
        validateEmployeeExists(dto.getEmployeeId());

        Contract entity = mapper.toEntity(dto); // String -> enum: IllegalArgumentException si inválido
        validateContractFields(entity);
        validateNoOverlap(entity, null); // null: no excluir ninguno (alta)

        Contract saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    public ContractDto update(Long id, ContractDto dto) {
        repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract " + id + " no existe"));
        validateEmployeeExists(dto.getEmployeeId());

        Contract updated = mapper.toEntity(dto);
        updated.setId(id);
        validateContractFields(updated);
        validateNoOverlap(updated, id); // excluir el propio id al comprobar solape

        Contract saved = repo.save(updated);
        return mapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Contract " + id + " no existe");
        repo.deleteById(id);
    }

    // ===== Helpers de validación =====

    private void validateEmployeeExists(Long employeeId) {
        if (employeeId == null || !employeeRepo.existsById(employeeId)) {
            throw new IllegalArgumentException("employeeId requerido y debe existir");
        }
    }

    private void validateContractFields(Contract c) {
        if (c.getType() == null) throw new IllegalArgumentException("type requerido");
        if (c.getStartDate() == null) throw new IllegalArgumentException("startDate requerido");
        if (c.getEndDate() != null && c.getEndDate().isBefore(c.getStartDate())) {
            throw new IllegalArgumentException("endDate no puede ser anterior a startDate");
        }
        if (c.getWeeklyHours() != null) {
            int h = c.getWeeklyHours();
            if (h < 1 || h > 60) throw new IllegalArgumentException("weeklyHours debe estar en 1..60");
        }
    }

    private void validateNoOverlap(Contract candidate, Long excludeId) {
        List<Contract> existing = repo.findByEmployeeId(candidate.getEmployee().getId());
        for (Contract other : existing) {
            if (excludeId != null && other.getId().equals(excludeId)) continue;
            if (datesOverlap(candidate.getStartDate(), candidate.getEndDate(),
                    other.getStartDate(), other.getEndDate())) {
                throw new ConflictException("Solape de contratos para el empleado en el periodo especificado");
            }
        }
    }

    /** Solape de fechas, considerando endDate == null como abierto. */
    private boolean datesOverlap(LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        LocalDate end1 = (e1 == null) ? LocalDate.MAX : e1;
        LocalDate end2 = (e2 == null) ? LocalDate.MAX : e2;
        return !end1.isBefore(s2) && !end2.isBefore(s1);
    }


    public List<ContractDto> listByEmployee(Long employeeId) {
        return repo.findByEmployeeId(employeeId)
                .stream().map(mapper::toDto).toList();
    }

}
