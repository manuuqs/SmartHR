package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEmployeeCompleteDto {

    @NotBlank(message = "Nombre requerido")
    @Size(max = 120)
    private String name;

    @NotBlank(message = "Apellidos requeridos")
    @Size(max = 120)
    private String surname;

    @NotBlank(message = "Email requerido")
    @Email
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Username requerido")
    private String username;

    @NotBlank(message = "Contraseña requerida")
    private String password;

    @NotBlank(message = "Ubicación requerida")
    @Size(max = 80)
    private String location;

    @NotNull(message = "Fecha de alta requerida")
    private LocalDate hireDate;

    @NotNull(message = "Departamento requerido")
    private Long departmentId;

    @NotBlank(message = "Puesto requerido")
    @Size(max = 120)
    private String jobPositionTitle;

    @NotNull(message = "Rol requerido")
    private String role;

    @NotNull(message = "Horas semanales requeridas")
    private Integer weeklyHours;

    // CONTRATO
    @NotNull(message = "Tipo de contrato requerido")
    private ContractTypeDto contractType;

    @NotNull(message = "Fecha inicio contrato requerida")
    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    // ASIGNACIÓN (opcional)
    private Long projectId;
    private String assignmentJobPosition;

    // SKILLS
    private List<Long> skillIds;

}

