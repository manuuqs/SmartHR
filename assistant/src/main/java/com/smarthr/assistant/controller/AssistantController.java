package com.smarthr.assistant.controller;

import com.smarthr.assistant.dto.ChatRequest;
import com.smarthr.assistant.service.RagService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final RagService ragService;  // ← Inyectar RagService

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        // ⭐ USAR RAG en lugar de chatClient directo
        return ragService.chatWithRag(request.message());
    }
}


