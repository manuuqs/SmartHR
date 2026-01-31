package com.smarthr.backend.service;

import com.smarthr.backend.domain.Project;
import com.smarthr.backend.repository.DepartmentRepository;
import com.smarthr.backend.repository.ProjectRepository;
import com.smarthr.backend.repository.SkillRepository;
import com.smarthr.backend.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagSnapshotService {

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestService leaveRequestService;
    private final EmployeeService employeeService;
    private final SkillRepository skillRepository;

    public CompanyRagSnapshotDto getCompanyRagSnapshot() {

        List<EmployeeCompleteDto> employees = employeeService.getEmployeesCompleteRag();

        List<ProjectRagDto> projects = projectRepository.findAll()
                .stream()
                .map(this::toProjectRag)
                .toList();

        List<DepartmentRagDto> departments = departmentRepository.findAll()
                .stream()
                .map(d -> new DepartmentRagDto(d.getName(), d.getDescription()))
                .toList();

        List<PendingLeaveRequestRagDto> pending = leaveRequestService.getPendingRequests()
                .stream()
                .map(lr -> new PendingLeaveRequestRagDto(
                        lr.getEmployeeName(),
                        lr.getStatus(),
                        lr.getType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        lr.getComments()
                ))
                .toList();

        List<SkillRagDto> skills = skillRepository.findAll()
                .stream()
                .map(s -> new SkillRagDto(
                        s.getName(),
                        s.getDescription()
                ))
                .toList();

        return new CompanyRagSnapshotDto(employees, projects, departments, skills, pending);
    }

    private ProjectRagDto toProjectRag(Project p) {
        return new ProjectRagDto(
                p.getCode(),
                p.getName(),
                p.getClient(),
                p.getUbication(),
                p.getStartDate(),
                p.getEndDate()
        );
    }
}
