package com.smarthr.assistant.service;


import com.smarthr.assistant.dto.EmployeeCompleteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;


    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void syncSmartHRData() {
        try {
            // Llamar endpoint protegido (necesitas auth temporal o endpoint público)
            ResponseEntity<List<EmployeeCompleteDto>> response =
                    restTemplate.exchange(
                            "http://backend:8080/api/employees/completeRag",
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );

            List<EmployeeCompleteDto> employees = response.getBody();
            List<Document> ragDocs = employees.stream()
                    .map(this::employeeToRagDocument)
                    .toList();

            vectorStore.delete(List.of());
            vectorStore.add(ragDocs);
            log.info("✅ RAG: {} empleados sincronizados del backend real", ragDocs.size());

        } catch (Exception e) {
            log.error("❌ Error RAG sync", e);
        }
    }

    private Document employeeToRagDocument(EmployeeCompleteDto emp) {
        return Document.builder()
                .text("""
            EMPLEADO: %s (%s)
            PUESTO: %s | DEPARTAMENTO: %s | UBICACIÓN: %s
            SKILLS: %s
            PROYECTOS: %s
            CONTRATO: %s | HORAS: %d
            SALARIO: %.2f€ %s
            FECHA CONTRATACIÓN: %s
            """.formatted(
                        emp.name(), emp.email(), emp.jobPosition(), emp.department(),
                        emp.location(), emp.skills(), emp.projects(), emp.contractType(),
                        emp.weeklyHours(), emp.baseSalary(),
                        emp.bonus() != null ? "+ " + emp.bonus() + "€ bonus" : "",
                        emp.hireDate()
                ))
                .metadata(Map.of(
                        "name", emp.name(),
                        "email", emp.email(),
                        "type", "employee"
                ))
                .build();
    }

    public String chatWithRag(String message) {
        List<Document> relevant = vectorStore.similaritySearch(message);
        String context = relevant.stream()
                .limit(3)
                .map(doc -> doc.getText())  // ← getText() NO getContent()
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClient.prompt()
                .system("Datos internos SmartHR:\n\n" + context)
                .user(message)
                .call()
                .content();
    }

}

