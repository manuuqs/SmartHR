package com.smarthr.backend.service;

import com.smarthr.backend.domain.Project;
import com.smarthr.backend.repository.AssignmentRepository;
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
    private final AssignmentRepository assignmentRepository;

    public CompanyRagSnapshotDto getCompanyRagSnapshot() {

        // 1️⃣ Traemos todos los proyectos del repositorio
        List<ProjectRagDto> allProjects = projectRepository.findAll()
                .stream()
                .map(this::toProjectRag)
                .toList();

        // 2️⃣ Traemos todos los empleados y asociamos sus proyectos completos
        List<EmployeeCompleteDto> employees = employeeService.getEmployeesCompleteRag()
                .stream()
                .map(emp -> {
                    // Recuperamos los proyectos completos del empleado
                    List<ProjectRagDto> employeeProjects = assignmentRepository.findByEmployeeId(emp.id())
                            .stream()
                            .map(a -> toProjectRag(a.getProject())) // convierte Project -> ProjectRagDto
                            .distinct()
                            .toList();

                    // Creamos un nuevo EmployeeCompleteDto con los proyectos completos
                    return new EmployeeCompleteDto(
                            emp.id(),
                            emp.name(),
                            emp.email(),
                            emp.location(),
                            emp.hireDate(),
                            emp.department(),
                            emp.jobPosition(),
                            emp.skills(),
                            employeeProjects,
                            emp.contractType(),
                            emp.weeklyHours(),
                            emp.contractStartDate(),
                            emp.contractEndDate(),
                            emp.baseSalary(),
                            emp.bonus(),
                            emp.leaveRequests()
                    );
                })
                .toList();

        // 3️⃣ El resto de la info (proyectos, departamentos, skills, ausencias) igual
        List<ProjectRagDto> projects = allProjects;

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

        // 4️⃣ Retornamos snapshot
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
