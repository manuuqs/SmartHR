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

        // Skills
        List<String> skills,

        // 🔥 IMPORTANTE: acepta "projects" del backend
        @JsonAlias("projects")
        List<ProjectRagDto> projectsInfo,

        // Contrato
        String contractType,
        Integer weeklyHours,
        LocalDate contractStartDate,
        LocalDate contractEndDate,

        // Salario
        BigDecimal baseSalary,
        BigDecimal bonus,

        // Ausencias
        List<String> leaveRequests
) {}
