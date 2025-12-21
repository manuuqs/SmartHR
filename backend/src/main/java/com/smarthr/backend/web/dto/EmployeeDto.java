
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO principal de empleado (lectura/escritura).
 * Incluye claves foráneas de Department y JobPosition, y campos auxiliares para mostrar.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 80)
    private String role;

    @Size(max = 80)
    private String location;

    @Email
    @Size(max = 180)
    private String email;

    private LocalDate hireDate;

    /** Clave foránea a Department. */
    private Long departmentId;

    /** Nombre del departamento (campo auxiliar solo lectura). */
    private String departmentName;

    /** Clave foránea a JobPosition. */
    private Long jobPositionId;

    /** Título del puesto (campo auxiliar solo lectura). */
    private String jobPositionTitle;

  }
