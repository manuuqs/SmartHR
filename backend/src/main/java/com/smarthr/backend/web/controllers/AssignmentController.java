package com.smarthr.backend.web.controllers;

import com.smarthr.backend.domain.Assignment;
import com.smarthr.backend.repository.AssignmentRepository;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentRepository repo;
    private final EmployeeRepository employeeRepo;
    private final ProjectRepository projectRepo;

    public AssignmentController(AssignmentRepository repo, EmployeeRepository employeeRepo, ProjectRepository projectRepo){
        this.repo = repo; this.employeeRepo = employeeRepo; this.projectRepo = projectRepo;
    }

    @PostMapping
    public ResponseEntity<Assignment> create(@RequestBody Assignment a){
        // validar employee y project existen
        if (a.getEmployee() == null || a.getEmployee().getId() == null) return ResponseEntity.badRequest().build();
        if (a.getProject() == null || a.getProject().getId() == null) return ResponseEntity.badRequest().build();

        var emp = employeeRepo.findById(a.getEmployee().getId()).orElse(null);
        var prj = projectRepo.findById(a.getProject().getId()).orElse(null);
        if (emp == null || prj == null) return ResponseEntity.badRequest().build();

        a.setEmployee(emp); a.setProject(prj);
        Assignment saved = repo.save(a);
        return ResponseEntity.created(URI.create("/api/assignments/"+saved.getId())).body(saved);
    }

    // list/get/update/delete similares al patrón
}
