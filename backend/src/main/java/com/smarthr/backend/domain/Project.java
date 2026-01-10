
package com.smarthr.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
