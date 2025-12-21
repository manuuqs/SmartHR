
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.web.dto.EmployeeDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmployeeMapper {

    @Mappings({
            @Mapping(source = "department.id",      target = "departmentId"),
            @Mapping(source = "department.name",    target = "departmentName"),
            @Mapping(source = "jobPosition.id",     target = "jobPositionId"),
            @Mapping(source = "jobPosition.title",  target = "jobPositionTitle")
    })
    EmployeeDto toDto(Employee entity);

    @Mappings({
            // Creamos referencias "por id" para relaciones (sin cargar toda la entidad)
            @Mapping(source = "departmentId", target = "department", qualifiedByName = "refDepartment"),
            @Mapping(source = "jobPositionId", target = "jobPosition", qualifiedByName = "refJobPosition"),
            // Campos auxiliares del DTO no se mapean al entity (name/title)
            @Mapping(target = "id",            source = "id"),
            @Mapping(target = "name",          source = "name"),
            @Mapping(target = "role",          source = "role"),
            @Mapping(target = "location",      source = "location"),
            @Mapping(target = "email",         source = "email"),
            @Mapping(target = "hireDate",      source = "hireDate")
    })
    Employee toEntity(EmployeeDto dto);

    @Named("refDepartment")
    default Department refDepartment(Long id) {
        if (id == null) return null;
        Department d = new Department();
        d.setId(id);
        return d;
    }

    @Named("refJobPosition")
    default JobPosition refJobPosition(Long id) {
        if (id == null) return null;
        JobPosition jp = new JobPosition();
        jp.setId(id);
        return jp;
    }
}
