package com.smarthr.assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeCompleteDto(
        Long id,
        String name,
        String email,
        String location,
        LocalDate hireDate,
        String department,
        String jobPosition,

        List<String> skills,

        @JsonAlias("projects")
        List<ProjectRagDto> projectsInfo,

        String contractType,
        Integer weeklyHours,
        LocalDate contractStartDate,
        LocalDate contractEndDate,

        BigDecimal baseSalary,
        BigDecimal bonus,

        List<String> leaveRequests
) {

    // Builder estático
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String location;
        private LocalDate hireDate;
        private String department;
        private String jobPosition;
        private List<String> skills;
        private List<ProjectRagDto> projectsInfo;
        private String contractType;
        private Integer weeklyHours;
        private LocalDate contractStartDate;
        private LocalDate contractEndDate;
        private BigDecimal baseSalary;
        private BigDecimal bonus;
        private List<String> leaveRequests;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder jobPosition(String jobPosition) {
            this.jobPosition = jobPosition;
            return this;
        }

        public Builder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public Builder projectsInfo(List<ProjectRagDto> projectsInfo) {
            this.projectsInfo = projectsInfo;
            return this;
        }

        public Builder contractType(String contractType) {
            this.contractType = contractType;
            return this;
        }

        public Builder weeklyHours(Integer weeklyHours) {
            this.weeklyHours = weeklyHours;
            return this;
        }

        public Builder contractStartDate(LocalDate contractStartDate) {
            this.contractStartDate = contractStartDate;
            return this;
        }

        public Builder contractEndDate(LocalDate contractEndDate) {
            this.contractEndDate = contractEndDate;
            return this;
        }

        public Builder baseSalary(BigDecimal baseSalary) {
            this.baseSalary = baseSalary;
            return this;
        }

        public Builder baseSalary(Double baseSalary) {
            this.baseSalary = baseSalary != null ? BigDecimal.valueOf(baseSalary) : null;
            return this;
        }

        public Builder bonus(BigDecimal bonus) {
            this.bonus = bonus;
            return this;
        }

        public Builder bonus(Double bonus) {
            this.bonus = bonus != null ? BigDecimal.valueOf(bonus) : null;
            return this;
        }

        public Builder leaveRequests(List<String> leaveRequests) {
            this.leaveRequests = leaveRequests;
            return this;
        }

        public EmployeeCompleteDto build() {
            return new EmployeeCompleteDto(
                    id, name, email, location, hireDate, department, jobPosition,
                    skills, projectsInfo, contractType, weeklyHours,
                    contractStartDate, contractEndDate, baseSalary, bonus, leaveRequests
            );
        }
    }
}