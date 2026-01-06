
package com.smarthr.backend.web.mapper;

import com.smarthr.backend.domain.Project;
import com.smarthr.backend.web.dto.ProjectDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProjectMapper {
    ProjectDto toDto(Project entity);
    Project toEntity(ProjectDto dto);
}
