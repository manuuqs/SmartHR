package com.smarthr.assistant.service;

import com.smarthr.assistant.component.SmartHRQueryRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmartHRAssistantService {

    private final SmartHRQueryRouter router;
    private final RagService ragService;
    private final AggregationService aggregationService;

    public String chat(String message) {

        return switch (router.classify(message)) {

            case SMALL_TALK ->
                    "Hola. Soy el asistente interno de SmartHR. ¿En qué puedo ayudarte?";

            case AGGREGATION ->
                    aggregationService.handle(message);

            case RAG ->
                    ragService.chatWithRag(message);
        };
    }
}

