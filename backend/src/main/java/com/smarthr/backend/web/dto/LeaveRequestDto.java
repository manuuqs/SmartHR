
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private Long id;

    private Long employeeId;
    private String employeeName;

    /** VACATION, SICKNESS, UNPAID, OTHER */
    @NotNull
    private String type;

    /** PENDING, APPROVED, REJECTED */
    @NotNull
    private String status;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String comments;
}
