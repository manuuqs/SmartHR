package com.smarthr.assistant.service;


import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
public class AggregationService {

    public String handle(String message) {

        if (message.matches("(?i).*cuántos.*empleados.*")) {
            return """
            No dispongo de información agregada exacta en este momento.
            Para conocer el número total de empleados, consulte el panel de RRHH
            o el sistema corporativo oficial.
            """;
        }

        return """
        No dispongo de información agregada para esa consulta.
        Por favor, contacte con RRHH o con el administrador del sistema.
        """;
    }
}


