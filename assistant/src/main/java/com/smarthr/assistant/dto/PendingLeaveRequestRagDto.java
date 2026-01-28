package com.smarthr.assistant.dto;

import java.time.LocalDate;

public record PendingLeaveRequestRagDto(
        String employeeName,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        String comments
) {}
