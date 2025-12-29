
package com.smarthr.backend.mapper;

import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.PerformanceReview;
import com.smarthr.backend.web.dto.PerformanceReviewDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PerformanceReviewMapper {

    @Mappings({
            @Mapping(source = "employee.id",   target = "employeeId"),
            @Mapping(source = "employee.name", target = "employeeName"),
            @Mapping(source = "rating",        target = "rating", qualifiedByName = "enumToStringRating")
    })
    PerformanceReviewDto toDto(PerformanceReview entity);

    @Mappings({
            @Mapping(source = "employeeId", target = "employee", qualifiedByName = "refEmployee"),
            @Mapping(source = "rating",     target = "rating",   qualifiedByName = "stringToEnumRating")
    })
    PerformanceReview toEntity(PerformanceReviewDto dto);

    @Named("refEmployee")
    default Employee refEmployee(Long id) {
        if (id == null) return null;
        Employee e = new Employee();
        e.setId(id);
        return e;
    }

    @Named("stringToEnumRating")
    default PerformanceReview.Rating stringToEnumRating(String value) {
        return value == null ? null : PerformanceReview.Rating.valueOf(value);
    }

    @Named("enumToStringRating")
    default String enumToStringRating(PerformanceReview.Rating rating) {
        return rating == null ? null : rating.name();
    }
}

