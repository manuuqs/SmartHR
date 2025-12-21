
package com.smarthr.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {

    public enum Rating { POOR, FAIR, GOOD, VERY_GOOD, EXCELLENT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING) @NotNull
    private Rating rating;

    @Size(max = 1000)
    private String comments;

}
