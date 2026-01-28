package com.smarthr.assistant.controller;

import com.smarthr.assistant.dto.ChatRequest;
import com.smarthr.assistant.service.RagService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final RagService ragService;

    @PostMapping("/chat")
    public String chat(
            @RequestBody ChatRequest request
           // @RequestHeader("Authorization") String authHeader  // ⭐ Token del front
    ) {

        //String token = authHeader.replace("Bearer ", "");
          return ragService.chatWithRag(request.message());
    }
}




