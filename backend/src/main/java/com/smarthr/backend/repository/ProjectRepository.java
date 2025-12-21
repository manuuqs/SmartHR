package com.smarthr.backend.repository;
import com.smarthr.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
