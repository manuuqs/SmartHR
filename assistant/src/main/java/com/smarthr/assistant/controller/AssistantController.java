package com.smarthr.assistant.controller;

import com.smarthr.assistant.service.RagService;  // Ajusta tu package
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import com.smarthr.assistant.dto.ChatRequest;
import com.smarthr.assistant.service.RagService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final RagService ragService;

//    @PostMapping("/chat")
//    public String chat(
//            @RequestBody ChatRequest request
//           // @RequestHeader("Authorization") String authHeader  // ⭐ Token del front
//    ) {
//
//        //String token = authHeader.replace("Bearer ", "");
//          return ragService.chatWithRag(request.message());
//    }
//    @PostMapping("/chat")
//    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
//        String message = request.getOrDefault("message", "sin mensaje");
//        System.out.println("🧠 Chat SmartHR: {}"+ message);
//
//        return ResponseEntity.ok(Map.of(
//                "response", "¡Hola " + message + "! SmartHR RAG listo ✅\n• Ollama: llama3.2:3b\n• PGVector: 25 docs\n• Backend OK"
//        ));
//    }
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "sin mensaje");
        System.out.println("🧠 Chat SmartHR RAG: {}" + message);

        try {
            String response = ragService.chatWithRag(message);  // ⭐ Tu método real

            return ResponseEntity.ok(Map.of("response", response));

        } catch (Exception e) {
            System.out.println("❌ Error RAG: "+ e);
            return ResponseEntity.ok(Map.of(
                    "response", "¡Hola! SmartHR básico OK ✅\nError RAG: " + e.getMessage()
            ));
        }
}


}




