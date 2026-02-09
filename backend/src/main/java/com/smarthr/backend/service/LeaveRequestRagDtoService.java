package com.smarthr.backend.service;

import com.smarthr.backend.domain.LeaveRequest;
import com.smarthr.backend.repository.LeaveRequestRepository;
import com.smarthr.backend.web.dto.LeaveRequestRagDto;
import com.smarthr.backend.web.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveRequestRagDtoService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequestRagDto buildLeaveRequestRag(Long leaveRequestId) {

        LeaveRequest lr = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("LeaveRequest not found: " + leaveRequestId));

        return new LeaveRequestRagDto(
                lr.getEmployee().getName(),
                lr.getStatus().name(),
                lr.getType().name(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getComments()
        );
    }
}
