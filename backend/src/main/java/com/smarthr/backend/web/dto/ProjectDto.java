
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;

    @NotBlank @Size(max = 40)
    private String code;

    @NotBlank @Size(max = 160)
    private String name;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 255)
    private String client;

    @Size(max = 255)
    private String ubication;
}
