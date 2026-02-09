package com.smarthr.backend.service;


import com.smarthr.backend.domain.LeaveRequest;
import com.smarthr.backend.web.dto.LeaveRequestRagDto;
import com.smarthr.backend.web.mapper.LeaveRequestMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.LeaveRequestRepository;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeaveRequestService {

    private final LeaveRequestRepository repo;
    private final EmployeeRepository employeeRepo;
    private final LeaveRequestMapper mapper;
    private final LeaveRequestRagDtoService responseRagDtoService;

    public LeaveRequestDto create(LeaveRequestDto dto) {
        var emp = employeeRepo.findById(dto.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("Empleado no existe"));
        LeaveRequest lr = mapper.toEntity(dto);
        lr.setEmployee(emp);
        if (lr.getEndDate().isBefore(lr.getStartDate())) throw new IllegalArgumentException("Fecha fin no puede ser anterior a inicio");
        LeaveRequest saved = repo.save(lr);
        return mapper.toDto(saved);
    }

    public LeaveRequestDto changeStatus(Long id, String status) {

        LeaveRequest.LeaveStatus newStatus;
        try {
            newStatus = LeaveRequest.LeaveStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + status);
        }

        LeaveRequest leaveRequest = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id " + id));

        leaveRequest.setStatus(newStatus);

        LeaveRequest saved = repo.save(leaveRequest);


        try {
            log.info("Actualizando LeaveRequest en RAG: {}, new status {}", leaveRequest, status);

            LeaveRequestRagDto ragDto =
                    responseRagDtoService.buildLeaveRequestRag(saved.getId());

            log.info("LeaveRequestRagDto: {}", ragDto);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(
                    "http://assistant:9090/internal/rag/upsert-leave-request",
                    ragDto,
                    Void.class
            );

        } catch (Exception e) {
            log.warn("⚠️ No se pudo actualizar LeaveRequest en RAG: {}", e.getMessage());
        }
        return mapper.toDto(saved);
    }


    public List<LeaveRequestDto> listByEmployee(Long employeeId) {
        return repo.findByEmployeeId(employeeId)
                .stream().map(mapper::toDto).toList();
    }

    public List<LeaveRequestDto> getPendingRequests() {
        List<LeaveRequest> pending = repo.findAll();
        return pending.stream()
                .map(mapper::toDto) // Usamos el mapper que ya tienes
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDto> listRecentByEmployee(Long employeeId, int limit) {
        return listByEmployee(employeeId).stream().limit(limit).toList();
    }



}
