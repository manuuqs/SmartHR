
package com.smarthr.backend.service;

import com.smarthr.backend.domain.PerformanceReview;
import com.smarthr.backend.mapper.PerformanceReviewMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.PerformanceReviewRepository;
import com.smarthr.backend.web.ConflictException;
import com.smarthr.backend.web.ResourceNotFoundException;
import com.smarthr.backend.web.dto.PerformanceReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceReviewService {

    private final PerformanceReviewRepository repo;
    private final EmployeeRepository employeeRepo;
    private final PerformanceReviewMapper mapper;

    @Transactional(readOnly = true)
    public Page<PerformanceReviewDto> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PerformanceReviewDto get(Long id) {
        PerformanceReview pr = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview " + id + " no existe"));
        return mapper.toDto(pr);
    }

    public PerformanceReviewDto create(PerformanceReviewDto dto) {
        validateEmployeeExists(dto.getEmployeeId());

        PerformanceReview entity = mapper.toEntity(dto); // rating: String -> enum (IllegalArgumentException si inválido)
        validateReviewFields(entity);

        if (repo.existsByEmployee_IdAndReviewDate(dto.getEmployeeId(), entity.getReviewDate())) {
            throw new ConflictException("Ya existe una evaluación para esa fecha y empleado");
        }

        PerformanceReview saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    public PerformanceReviewDto update(Long id, PerformanceReviewDto dto) {
        repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("PerformanceReview " + id + " no existe"));
        validateEmployeeExists(dto.getEmployeeId());

        PerformanceReview updated = mapper.toEntity(dto);
        updated.setId(id);
        validateReviewFields(updated);

        boolean conflict = repo.existsByEmployee_IdAndReviewDate(dto.getEmployeeId(), updated.getReviewDate());
        if (conflict) {
            throw new ConflictException("Ya existe una evaluación para esa fecha y empleado");
        }

        PerformanceReview saved = repo.save(updated);
        return mapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("PerformanceReview " + id + " no existe");
        repo.deleteById(id);
    }

    // ===== Helpers =====
    private void validateEmployeeExists(Long employeeId) {
        if (employeeId == null || !employeeRepo.existsById(employeeId)) {
            throw new IllegalArgumentException("employeeId requerido y debe existir");
        }
    }

    private void validateReviewFields(PerformanceReview pr) {
        if (pr.getRating() == null) throw new IllegalArgumentException("rating requerido");
        if (pr.getReviewDate() == null) throw new IllegalArgumentException("reviewDate requerido");
        if (pr.getReviewDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("reviewDate no puede ser futura");
        }
        if (pr.getComments() != null && pr.getComments().length() > 1000) {
            throw new IllegalArgumentException("comments supera longitud máxima (1000)");
        }
    }
}
