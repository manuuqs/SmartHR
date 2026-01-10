
package com.smarthr.backend.web.mapper;

import com.smarthr.backend.domain.Assignment;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.Project;
import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.web.dto.AssignmentDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AssignmentMapper {

    @Mappings({
            @Mapping(source = "employee.id",    target = "employeeId"),
            @Mapping(source = "employee.name",  target = "employeeName"),
            @Mapping(source = "project.id",     target = "projectId"),
            @Mapping(source = "project.code",   target = "projectCode"),
            @Mapping(source = "project.name",   target = "projectName"),
            @Mapping(source = "jobPosition.title", target = "jobPosition") // Convertimos entidad a String
    })
    AssignmentDto toDto(Assignment entity);

    @Mappings({
            @Mapping(source = "employeeId",   target = "employee",    qualifiedByName = "refEmployee"),
            @Mapping(source = "projectId",    target = "project",     qualifiedByName = "refProject"),
            @Mapping(source = "jobPosition",  target = "jobPosition", qualifiedByName = "refJobPosition") // Convertimos String a entidad
    })
    Assignment toEntity(AssignmentDto dto);

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
