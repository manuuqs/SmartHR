package com.smarthr.assistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                Eres el asistente IA de SmartHR. 
                Ayudas con dudas de RRHH, gestión de empleados, contratos, proyectos y políticas internas.
                Responde de forma clara y profesional en español.
                """)
                .build();
    }
}
