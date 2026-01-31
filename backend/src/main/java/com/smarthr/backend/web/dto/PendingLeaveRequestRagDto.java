package com.smarthr.backend.web.dto;

import java.time.LocalDate;

public record PendingLeaveRequestRagDto(
        String employeeName,
        String status,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        String comments
) {}
