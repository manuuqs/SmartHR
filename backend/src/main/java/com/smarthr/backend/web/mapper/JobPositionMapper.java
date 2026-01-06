package com.smarthr.backend.web.mapper;

import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.web.dto.JobPositionDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface JobPositionMapper {
    JobPositionDto toDto(JobPosition entity);
    JobPosition toEntity(JobPositionDto dto);
}
