package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO de Departamento. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDto {
    private Long id;

    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

}
