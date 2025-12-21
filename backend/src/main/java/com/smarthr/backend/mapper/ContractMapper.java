
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Contract;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.web.dto.ContractDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ContractMapper {

    @Mappings({
            @Mapping(source = "employee.id",  target = "employeeId"),
            @Mapping(source = "employee.name",target = "employeeName"),
            @Mapping(source = "type",         target = "type", qualifiedByName = "enumToString")
    })
    ContractDto toDto(Contract entity);

    @Mappings({
            @Mapping(source = "employeeId",   target = "employee", qualifiedByName = "refEmployee"),
            @Mapping(source = "type",         target = "type",     qualifiedByName = "stringToEnum")
    })
    Contract toEntity(ContractDto dto);

    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    @Named("stringToEnum")
    default Contract.ContractType stringToEnum(String value) {
        if (value == null) return null;
        return Contract.ContractType.valueOf(value);
    }

    @Named("enumToString")
    default String enumToString(Contract.ContractType type) {
        return type == null ? null : type.name();
    }
}
