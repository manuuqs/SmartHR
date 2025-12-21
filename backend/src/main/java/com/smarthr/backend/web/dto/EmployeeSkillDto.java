
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillDto {
    private Long id;

    private Long employeeId;
    private Long skillId;

    private String skillName; // auxiliar

    @Min(1) @Max(5)
    private int level;
}
