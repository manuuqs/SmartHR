
package com.smarthr.backend.web.mapper;

import com.smarthr.backend.domain.Department;
import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.JobPosition;
import com.smarthr.backend.web.dto.EmployeeDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmployeeMapper {

    // ====== Lectura ======
    @Mappings({
            @Mapping(source = "department.id",      target = "departmentId"),
            @Mapping(source = "department.name",    target = "departmentName"),
            @Mapping(source = "jobPosition.id",     target = "jobPositionId"),
            @Mapping(source = "jobPosition.title",  target = "jobPositionTitle")
    })
    EmployeeDto toDto(Employee entity);

    // ====== Escritura (create / reemplazo completo) ======
    @Mappings({
            // Relaciones por id (no cargamos todo el agregado)
            @Mapping(source = "departmentId", target = "department",  qualifiedByName = "refDepartment"),
            @Mapping(source = "jobPositionId", target = "jobPosition", qualifiedByName = "refJobPosition"),
            // Campos auxiliares que no deben mapear hacia entity (name/title del agregado)
            @Mapping(target = "id",        source = "id"),
            @Mapping(target = "name",      source = "name"),
            @Mapping(target = "jobPosition.title", source = "jobPositionTitle"),
            @Mapping(target = "location",  source = "location"),
            @Mapping(target = "email",     source = "email"),
            @Mapping(target = "hireDate",  source = "hireDate")
    })
    Employee toEntity(EmployeeDto dto);

    // ====== Actualización parcial (PATCH-like): ignora nulls ======
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mappings({
            // No tocar el id del target
            @Mapping(target = "id", ignore = true),

            // Si el DTO trae departmentId != null, actualiza; si trae null, NO toca el department
            @Mapping(source = "departmentId", target = "department",  qualifiedByName = "refDepartment"),
            // Si el DTO trae jobPositionId != null, actualiza; si trae null, NO toca el jobPosition
            @Mapping(source = "jobPositionId", target = "jobPosition", qualifiedByName = "refJobPosition")
    })
    void updateEntityFromDto(EmployeeDto dto, @MappingTarget Employee entity);

    // ====== (Opcional) Actualización parcial “borrar con null” ======
    // Si quieres que null en el DTO signifique "quitar" el valor en la entidad (SET_TO_NULL),
    // usa este método en el servicio cuando proceda.
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "departmentId", target = "department",  qualifiedByName = "refDepartment"),
            @Mapping(source = "jobPositionId", target = "jobPosition", qualifiedByName = "refJobPosition")
    })
    void updateEntityFromDtoAllowNulls(EmployeeDto dto, @MappingTarget Employee entity);

    // ====== Helpers para referencias por id ======
    @Named("refDepartment")
    default Department refDepartment(Long id) {
        if (id == null) return null; // con IGNORE no se aplicará si el dto trae null
        Department d = new Department();
        d.setId(id);
        return d;
    }

    @Named("refJobPosition")
    default JobPosition refJobPosition(Long id) {
        if (id == null) return null; // con IGNORE no se aplicará si el dto trae null
        JobPosition jp = new JobPosition();
        jp.setId(id);
        return jp;
    }
}
