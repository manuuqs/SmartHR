package com.smarthr.backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // Ejemplo: "ROLE_RRHH"
    private Long employeeId;

}

