package com.smarthr.assistant.dto;

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

        // Proyectos
        List<String> projects,

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