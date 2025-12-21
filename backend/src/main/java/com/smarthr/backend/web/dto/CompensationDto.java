
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationDto {
    private Long id;

    private Long employeeId;
    private String employeeName;

    @NotNull
    private BigDecimal baseSalary;

    private BigDecimal bonus;
    private LocalDate effectiveFrom;
}
