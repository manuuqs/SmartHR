
package com.smarthr.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {
    private Long id;

    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
