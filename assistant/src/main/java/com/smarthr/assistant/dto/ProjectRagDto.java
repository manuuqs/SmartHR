package com.smarthr.assistant.dto;



import java.time.LocalDate;


public record ProjectRagDto(
        String code,
        String name,
        String client,
        String ubication,
        LocalDate startDate,
        LocalDate endDate
) {

}
