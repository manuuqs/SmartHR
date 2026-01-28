package com.smarthr.backend.web.dto;

import java.util.List;

public record CompanyRagSnapshotDto(
        List<EmployeeCompleteDto> employees,
        List<ProjectRagDto> projects,
        List<DepartmentRagDto> departments,
        List<SkillRagDto> skills,
        List<PendingLeaveRequestRagDto> pendingLeaveRequests
) {}

