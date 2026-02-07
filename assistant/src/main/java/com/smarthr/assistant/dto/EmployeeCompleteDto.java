package com.smarthr.assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeCompleteDto(
        Long id,
        String name,
        String email,
        String location,
        LocalDate hireDate,
        String department,
        String jobPosition,

        List<String> skills,

        @JsonAlias("projects")
        List<ProjectRagDto> projectsInfo,

        String contractType,
        Integer weeklyHours,
        LocalDate contractStartDate,
        LocalDate contractEndDate,

        BigDecimal baseSalary,
        BigDecimal bonus,

        List<String> leaveRequests
) {}
