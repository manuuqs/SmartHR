package com.smarthr.assistant.dto;


import java.time.LocalDate;

public record EmployeeRagDto(
        Long id,
        String name,
        String location,
        String email,
        LocalDate hireDate,
        Long departmentId,
        String departmentName,
        Long jobPositionId,
        String jobPositionTitle
) {


}
