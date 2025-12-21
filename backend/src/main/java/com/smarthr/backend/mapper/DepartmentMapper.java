
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.web.dto.DepartmentDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DepartmentMapper {
    DepartmentDto toDto(Department entity);
    Department toEntity(DepartmentDto dto);
}
