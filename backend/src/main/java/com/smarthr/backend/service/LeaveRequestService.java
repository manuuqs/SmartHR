package com.smarthr.backend.service;


import com.smarthr.backend.domain.LeaveRequest;
import com.smarthr.backend.web.mapper.LeaveRequestMapper;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.LeaveRequestRepository;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository repo;
    private final EmployeeRepository employeeRepo;
    private final LeaveRequestMapper mapper;

    public LeaveRequestDto create(LeaveRequestDto dto) {
        var emp = employeeRepo.findById(dto.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("Empleado no existe"));
        LeaveRequest lr = mapper.toEntity(dto);
        lr.setEmployee(emp);
        if (lr.getEndDate().isBefore(lr.getStartDate())) throw new IllegalArgumentException("Fecha fin no puede ser anterior a inicio");
        LeaveRequest saved = repo.save(lr);
        return mapper.toDto(saved);
    }

    public LeaveRequestDto changeStatus(Long id, String status) {
        LeaveRequest lr = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no existe"));
        lr.setStatus(LeaveRequest.LeaveStatus.valueOf(status.toUpperCase()));
        return mapper.toDto(repo.save(lr));
    }


    public List<LeaveRequestDto> listByEmployee(Long employeeId) {
        return repo.findByEmployeeId(employeeId)
                .stream().map(mapper::toDto).toList();
    }

    public List<LeaveRequestDto> getPendingRequests() {
        List<LeaveRequest> pending = repo.findByStatus(LeaveRequest.LeaveStatus.PENDING);
        return pending.stream()
                .map(mapper::toDto) // Usamos el mapper que ya tienes
                .collect(Collectors.toList());
    }

}
