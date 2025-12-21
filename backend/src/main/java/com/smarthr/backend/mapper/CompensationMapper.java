
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Compensation;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.web.dto.CompensationDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CompensationMapper {

    @Mappings({
            @Mapping(source = "employee.id",  target = "employeeId"),
            @Mapping(source = "employee.name",target = "employeeName")
    })
    CompensationDto toDto(Compensation entity);

    @Mappings({
            @Mapping(source = "employeeId",   target = "employee", qualifiedByName = "refEmployee")
    })
    Compensation toEntity(CompensationDto dto);

    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }
}
