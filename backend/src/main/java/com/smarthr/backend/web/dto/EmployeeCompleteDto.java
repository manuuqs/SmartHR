package com.smarthr.backend.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa un empleado completo, incluyendo sus habilidades,
 * ausencias y proyectos completos asociados.
 */
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

        // Proyectos completos del empleado
        List<ProjectRagDto> projects,

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
