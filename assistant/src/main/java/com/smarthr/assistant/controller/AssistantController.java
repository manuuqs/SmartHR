package com.smarthr.assistant.controller;

import java.util.Map;

import com.smarthr.assistant.service.SmartHRAssistantService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {


    private final SmartHRAssistantService assistantService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {

        String message = request.getOrDefault("message", "").trim();
        System.out.println("🧠 Chat SmartHR RAG: " + message);

        try {
            String response = assistantService.chat(message);
            return ResponseEntity.ok(Map.of("response", response));

        } catch (Exception e) {
            System.out.println("❌ Error RAG: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "response", "El asistente no está disponible en este momento. Contacte con el administrador del sistema."
            ));
        }
    }


}




