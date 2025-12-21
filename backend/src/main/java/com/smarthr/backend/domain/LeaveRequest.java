
package com.smarthr.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    public enum LeaveType { VACATION, SICKNESS, UNPAID, OTHER }
    public enum LeaveStatus { PENDING, APPROVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING) @NotNull
    private LeaveType type;

    @Enumerated(EnumType.STRING) @NotNull
    private LeaveStatus status = LeaveStatus.PENDING;

    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;

    @Column(length = 255)
    private String comments;

}
