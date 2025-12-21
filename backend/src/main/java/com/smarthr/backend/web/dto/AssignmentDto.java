
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {
    private Long id;

    private Long employeeId;
    private String employeeName;

    private Long projectId;
    private String projectCode;
    private String projectName;

    @NotBlank @Size(max = 100)
    private String roleOnProject;

    private LocalDate startDate;
    private LocalDate endDate;
}
