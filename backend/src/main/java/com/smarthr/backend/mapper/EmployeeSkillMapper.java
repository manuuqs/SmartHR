
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.EmployeeSkill;
import com.smarthr.backend.domain.Skill;
import com.smarthr.backend.web.dto.EmployeeSkillDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmployeeSkillMapper {

    @Mappings({
            @Mapping(source = "employee.id",  target = "employeeId"),
            @Mapping(source = "skill.id",     target = "skillId"),
            @Mapping(source = "skill.name",   target = "skillName")
    })
    EmployeeSkillDto toDto(EmployeeSkill entity);

    @Mappings({
            @Mapping(source = "employeeId",   target = "employee", qualifiedByName = "refEmployee"),
            @Mapping(source = "skillId",      target = "skill",    qualifiedByName = "refSkill")
    })
    EmployeeSkill toEntity(EmployeeSkillDto dto);

    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    @Named("refSkill")
    default Skill refSkill(Long id) {
        if (id == null) return null;
        Skill s = new Skill();
        s.setId(id);
        return s;
    }
}
