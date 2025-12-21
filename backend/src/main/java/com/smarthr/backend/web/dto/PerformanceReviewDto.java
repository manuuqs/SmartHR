
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewDto {
    private Long id;

    private Long employeeId;
    private String employeeName;

    private LocalDate reviewDate;

    /** POOR, FAIR, GOOD, VERY_GOOD, EXCELLENT */
    @NotNull
    private String rating;

    @Size(max = 1000)
    private String comments;
}
