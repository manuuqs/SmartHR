
package com.smarthr.backend.service;

import com.smarthr.backend.domain.Compensation;
import com.smarthr.backend.web.mapper.CompensationMapper;
import com.smarthr.backend.repository.CompensationRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.web.exceptions.ConflictException;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.dto.CompensationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompensationService {

    private final CompensationRepository repo;
    private final EmployeeRepository employeeRepo;
    private final CompensationMapper mapper;

    @Transactional(readOnly = true)
    public Page<CompensationDto> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CompensationDto get(Long id) {
        Compensation c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compensation " + id + " no existe"));
        return mapper.toDto(c);
    }

    public CompensationDto create(CompensationDto dto) {
        validateEmployeeExists(dto.getEmployeeId());
        Compensation entity = mapper.toEntity(dto);
        validateCompensationFields(entity);

        // Unicidad (employeeId, effectiveFrom)
        if (repo.existsByEmployee_IdAndEffectiveFrom(dto.getEmployeeId(), entity.getEffectiveFrom())) {
            throw new ConflictException("Ya existe una compensación con esa fecha efectiva para el empleado");
        }

        Compensation saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    public CompensationDto update(Long id, CompensationDto dto) {
        repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Compensation " + id + " no existe"));
        validateEmployeeExists(dto.getEmployeeId());

        Compensation updated = mapper.toEntity(dto);
        updated.setId(id);
        validateCompensationFields(updated);

        // Si cambiaste effectiveFrom, valida que no exista otro registro con esa fecha
        boolean conflict = repo.existsByEmployee_IdAndEffectiveFrom(dto.getEmployeeId(), updated.getEffectiveFrom());
        if (conflict) {
            // Podría ser el mismo registro; para precisión, compara id si implementas query que excluya el actual.
            throw new ConflictException("Ya existe una compensación con esa fecha efectiva para el empleado");
        }

        Compensation saved = repo.save(updated);
        return mapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Compensation " + id + " no existe");
        repo.deleteById(id);
    }

    // ===== Helpers =====
    private void validateEmployeeExists(Long employeeId) {
        if (employeeId == null || !employeeRepo.existsById(employeeId)) {
            throw new IllegalArgumentException("employeeId requerido y debe existir");
        }
    }

    private void validateCompensationFields(Compensation c) {
        if (c.getEffectiveFrom() == null) throw new IllegalArgumentException("effectiveFrom requerido");
        if (c.getBaseSalary() == null || isNegative(c.getBaseSalary())) {
            throw new IllegalArgumentException("baseSalary requerido y debe ser >= 0");
        }
        if (c.getBonus() != null && isNegative(c.getBonus())) {
            throw new IllegalArgumentException("bonus debe ser >= 0");
        }
    }

    private boolean isNegative(BigDecimal value) {
        return value.signum() < 0;
    }

    public List<CompensationDto> listByEmployee(Long employeeId) {
        return repo.findByEmployeeId(employeeId)
                .stream().map(mapper::toDto).toList();
    }


}
