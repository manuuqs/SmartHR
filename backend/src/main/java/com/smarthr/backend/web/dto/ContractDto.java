
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;

    private Long employeeId;
    private String employeeName;

    /** PERMANENT, TEMPORARY, INTERN, FREELANCE */
    @NotNull
    private String type;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer weeklyHours;
}
