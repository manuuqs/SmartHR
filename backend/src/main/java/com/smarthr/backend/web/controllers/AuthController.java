package com.smarthr.backend.web.controllers;

import com.smarthr.backend.service.AuthService;
import com.smarthr.backend.web.dto.LoginRequest;
import com.smarthr.backend.web.dto.LoginResponse;
import com.smarthr.backend.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            System.out.println("token: " + token);
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inesperado: " + e.getMessage());
            }
        }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword(), request.getRole());
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/delete/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        authService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }


}
