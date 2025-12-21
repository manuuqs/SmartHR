
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPositionDto {
    private Long id;

    @NotBlank @Size(max = 120)
    private String title;

    @Size(max = 255)
    private String description;
}
