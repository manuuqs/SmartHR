
package com.smarthr.backend.web.mapper;

import com.smarthr.backend.domain.Assignment;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.Project;
import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.web.dto.AssignmentDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { ProjectMapper.class }, builder = @Builder(disableBuilder = true))
public interface AssignmentMapper {

    // Convierte entidad -> DTO
    @Mappings({
            @Mapping(source = "employee.id", target = "employeeId"),
            @Mapping(source = "employee.name", target = "employeeName"),
            @Mapping(source = "project", target = "project"), // Usa ProjectMapper
            @Mapping(source = "jobPosition.title", target = "jobPosition")
    })
    AssignmentDto toDto(Assignment entity);

    // Convierte DTO -> entidad
    @Mappings({
            @Mapping(source = "employeeId", target = "employee", qualifiedByName = "refEmployee"),
            @Mapping(source = "project.id", target = "project", qualifiedByName = "refProject"),
            @Mapping(source = "jobPosition", target = "jobPosition", qualifiedByName = "refJobPosition")
    })
    Assignment toEntity(AssignmentDto dto);

    // Métodos auxiliares para referencias
    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    @Named("refProject")
    default Project refProject(Long id) {
        if (id == null) return null;
        Project p = new Project();
        p.setId(id);
        return p;
    }

    @Named("refJobPosition")
    default JobPosition refJobPosition(String title) {
        if (title == null) return null;
        JobPosition jp = new JobPosition();
        jp.setTitle(title);
        return jp;
    }
}
