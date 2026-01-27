package com.smarthr.assistant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeCompleteDto(
        String name,
        String email,
        String location,
        LocalDate hireDate,
        String department,
        String jobPosition,
        List<String> skills,
        List<String> projects,
        String contractType,
        Integer weeklyHours,
        BigDecimal baseSalary,
        BigDecimal bonus
) {}
