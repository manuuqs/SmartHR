
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.LeaveRequest;
import com.smarthr.backend.web.dto.LeaveRequestDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface LeaveRequestMapper {

    @Mappings({
            @Mapping(source = "employee.id",    target = "employeeId"),
            @Mapping(source = "employee.name",  target = "employeeName"),
            @Mapping(source = "type",           target = "type",   qualifiedByName = "enumToStringType"),
            @Mapping(source = "status",         target = "status", qualifiedByName = "enumToStringStatus")
    })
    LeaveRequestDto toDto(LeaveRequest entity);

    @Mappings({
            @Mapping(source = "employeeId",     target = "employee", qualifiedByName = "refEmployee"),
            @Mapping(source = "type",           target = "type",     qualifiedByName = "stringToEnumType"),
            @Mapping(source = "status",         target = "status",   qualifiedByName = "stringToEnumStatus")
    })
    LeaveRequest toEntity(LeaveRequestDto dto);

    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    @Named("stringToEnumType")
    default LeaveRequest.LeaveType stringToEnumType(String value) {
        return value == null ? null : LeaveRequest.LeaveType.valueOf(value);
    }

    @Named("stringToEnumStatus")
    default LeaveRequest.LeaveStatus stringToEnumStatus(String value) {
        return value == null ? null : LeaveRequest.LeaveStatus.valueOf(value);
    }

    @Named("enumToStringType")
    default String enumToStringType(LeaveRequest.LeaveType type) {
        return type == null ? null : type.name();
    }

    @Named("enumToStringStatus")
    default String enumToStringStatus(LeaveRequest.LeaveStatus status) {
        return status == null ? null : status.name();
    }
}
