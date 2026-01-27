package com.smarthr.assistant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeDto(
        String name, String email, String location, LocalDate hireDate,
        String department, String jobPosition, String contractType, Integer weeklyHours,
        BigDecimal baseSalary, BigDecimal bonus,
        List<String> skills, List<String> projects
) {}
