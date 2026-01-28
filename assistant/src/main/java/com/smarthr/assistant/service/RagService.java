package com.smarthr.assistant.service;


import com.smarthr.assistant.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final RestTemplate restTemplate;
    private final ChatClient chatClient;

    @EventListener(ApplicationReadyEvent.class)
    public void syncSmartHRData() {

        for (int i = 0; i < 6; i++) {
            try {
                log.info("🔄 Sincronizando RAG SmartHR... ({}/6)", i + 1);

                ResponseEntity<CompanyRagSnapshotDto> response =
                        restTemplate.exchange(
                                "http://backend:8080/public/completeRag",
                                HttpMethod.GET,
                                null,
                                CompanyRagSnapshotDto.class
                        );

                CompanyRagSnapshotDto snapshot = response.getBody();
                if (snapshot == null) {
                    log.warn("⚠️ Snapshot vacío");
                    return;
                }

                // 🔥 BORRAR SOLO DOCUMENTOS DE SMARTHR
                vectorStore.delete(
                        vectorStore.similaritySearch("smarthr").stream()
                                .filter(d -> "smarthr".equals(d.getMetadata().get("source")))
                                .map(Document::getId)
                                .toList()
                );

                List<Document> documents = buildDocuments(snapshot);
                vectorStore.add(documents);

                log.info("✅ RAG sincronizado: {} documentos", documents.size());
                return;

            } catch (Exception e) {
                log.warn("⏳ Backend no listo: {}", e.getMessage());
                sleep(5000);
            }
        }

        log.error("❌ FALLÓ sync RAG");
    }

    private List<Document> buildDocuments(CompanyRagSnapshotDto snapshot) {

        List<Document> docs = new ArrayList<>();

        snapshot.employees().forEach(e ->
                docs.add(employeeToDoc(e)));

        snapshot.projects().forEach(p ->
                docs.add(projectToDoc(p)));

        snapshot.skills().forEach(s ->
                docs.add(skillToDoc(s)));

        snapshot.departments().forEach(d ->
                docs.add(departmentToDoc(d)));

        snapshot.pendingLeaveRequests().forEach(l ->
                docs.add(leaveRequestToDoc(l)));

        return docs;
    }

    private Document employeeToDoc(EmployeeCompleteDto emp) {
        return Document.builder()
                .text(buildEmployeeText(emp))
                .metadata(Map.of(
                        "type", "EMPLOYEE",
                        "name", emp.name(),
                        "department", emp.department(),
                        "source", "smarthr"
                ))
                .build();
    }

    private Document projectToDoc(ProjectRagDto p) {
        return Document.builder()
                .text("""
                Proyecto de la empresa SmartHR.

                Nombre del proyecto: %s.
                Código interno: %s.
                Cliente: %s.
                Ubicación: %s.
                Fecha de inicio: %s.
                %s
                """.formatted(
                        p.name(),
                        p.code(),
                        p.client(),
                        p.ubication(),
                        p.startDate(),
                        p.endDate() != null
                                ? "Fecha de finalización: " + p.endDate() + "."
                                : "El proyecto se encuentra activo actualmente."
                ))
                .metadata(Map.of(
                        "type", "PROJECT",
                        "name", p.name(),
                        "client", p.client(),
                        "location", p.ubication(),
                        "source", "smarthr"
                ))
                .build();
    }

    private Document skillToDoc(SkillRagDto s) {
        return Document.builder()
                .text("""
                Habilidad disponible en la empresa SmartHR.

                Nombre de la habilidad: %s.
                Descripción: %s.
                """.formatted(
                        s.name(),
                        s.description()
                ))
                .metadata(Map.of(
                        "type", "SKILL",
                        "name", s.name(),
                        "source", "smarthr"
                ))
                .build();
    }


    private Document departmentToDoc(DepartmentRagDto d) {
        return Document.builder()
                .text("""
                Departamento de la empresa SmartHR.

                Nombre del departamento: %s.
                Descripción: %s.
                """.formatted(
                        d.name(),
                        d.description()
                ))
                .metadata(Map.of(
                        "type", "DEPARTMENT",
                        "name", d.name(),
                        "source", "smarthr"
                ))
                .build();
    }

    private Document leaveRequestToDoc(PendingLeaveRequestRagDto l) {
        return Document.builder()
                .text("""
                Solicitud de ausencia pendiente en la empresa SmartHR.

                Empleado: %s.
                Tipo de solicitud: %s.
                Periodo solicitado: desde %s hasta %s.
                Comentarios adicionales: %s.
                """.formatted(
                        l.employeeName(),
                        l.type(),
                        l.startDate(),
                        l.endDate(),
                        l.comments() != null ? l.comments() : "Sin comentarios adicionales"
                ))
                .metadata(Map.of(
                        "type", "LEAVE_REQUEST",
                        "employee", l.employeeName(),
                        "leaveType", l.type(),
                        "source", "smarthr"
                ))
                .build();
    }


    private String buildEmployeeText(EmployeeCompleteDto emp) {
        return """
        Empleado de la empresa SmartHR.

        Nombre: %s.
        Puesto: %s en el departamento de %s.
        Ubicación: %s.
        Fecha de contratación: %s.

        Habilidades: %s.
        Proyectos en los que participa: %s.

        Tipo de contrato: %s.
        Jornada semanal: %d horas.
        Salario base: %.2f euros %s.
        """.formatted(
                emp.name(),
                emp.jobPosition(),
                emp.department(),
                emp.location(),
                emp.hireDate(),
                emp.skills().isEmpty() ? "No especificadas" : String.join(", ", emp.skills()),
                emp.projects().isEmpty() ? "Ninguno" : String.join(", ", emp.projects()),
                emp.contractType(),
                emp.weeklyHours(),
                emp.baseSalary(),
                emp.bonus() != null ? "con un bonus de " + emp.bonus() + " euros" : ""
        );
    }

    public String chatWithRag(String message) {

        List<Document> relevant = vectorStore.similaritySearch(message);

        if (relevant.isEmpty()) {
            return "No dispongo de información interna suficiente para responder a esa pregunta.";
        }

        String context = relevant.stream()
                .limit(5)
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClient.prompt()
                .system("""
                Eres el asistente interno de SmartHR.
                Responde SOLO usando la información proporcionada.
                Si no tienes datos suficientes, indícalo claramente.

                INFORMACIÓN INTERNA:
                """ + context)
                .user(message)
                .call()
                .content();
    }


    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}



