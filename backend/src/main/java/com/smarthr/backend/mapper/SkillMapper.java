
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Skill;
import com.smarthr.backend.web.dto.SkillDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SkillMapper {
    SkillDto toDto(Skill entity);
    Skill toEntity(SkillDto dto);
}
