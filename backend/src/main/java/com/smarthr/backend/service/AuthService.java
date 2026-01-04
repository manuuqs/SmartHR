package com.smarthr.backend.service;


import com.smarthr.backend.domain.Employee;
import com.smarthr.backend.domain.User;
import com.smarthr.backend.repository.EmployeeRepository;
import com.smarthr.backend.repository.UserRepository;
import com.smarthr.backend.utils.JwtUtil;
import com.smarthr.backend.web.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!new BCryptPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return jwtUtil.generateToken(user.getUsername(), user.getRoles());
    }


    public void register(String username, String password, String role, Long employeeId) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role)); // Asigna el rol indicado
        user.setEmployee(employee);
        userRepository.save(user);
    }


    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        userRepository.delete(user);
    }


}

