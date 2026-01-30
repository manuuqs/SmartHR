package com.smarthr.assistant.service;

import com.smarthr.assistant.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final RestTemplate restTemplate;

    @Autowired
    private ChatClient chatClient;  // Spring AI

    @Autowired
    private VectorStore vectorStore;  // PGVector

    //@EventListener(ApplicationReadyEvent.class)
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

                List<Document> documents = buildDocuments(snapshot);
                upsertDocuments(documents);

                log.info("✅ RAG sincronizado: {} documentos", documents.size());
                return;

            } catch (Exception e) {
                log.warn("⏳ Backend no listo: {}", e.getMessage());
                sleep(5000);
            }
        }

        log.error("❌ FALLÓ sync RAG");
    }

    /* ==========================
       🔁 UPSERT REAL (por entityId en metadata)
       ========================== */
    private void upsertDocuments(List<Document> documents) {
        for (Document doc : documents) {
            try {
                // Obtener entityId de metadata
                String entityId = (String) doc.getMetadata().get("entityId");
                if (entityId != null) {
                    // Borra por ID exacto (el vectorStore usa metadata "entityId" como ID)
                    vectorStore.delete(List.of(entityId));
                }
            } catch (Exception ignored) {
                // No existía → OK
            }
            // Insertar el documento
            vectorStore.add(List.of(doc));
        }
    }


    /* ==========================
       🧱 BUILD DOCUMENTS
       ========================== */
    private List<Document> buildDocuments(CompanyRagSnapshotDto snapshot) {
        List<Document> docs = new ArrayList<>();
        snapshot.employees().forEach(e -> docs.add(employeeToDoc(e)));
        snapshot.projects().forEach(p -> docs.add(projectToDoc(p)));
        snapshot.skills().forEach(s -> docs.add(skillToDoc(s)));
        snapshot.departments().forEach(d -> docs.add(departmentToDoc(d)));
        snapshot.pendingLeaveRequests().forEach(l -> docs.add(leaveRequestToDoc(l)));
        return docs;
    }

    /* ==========================
       👨‍💼 EMPLOYEE
       ========================== */
    private Document employeeToDoc(EmployeeCompleteDto emp) {
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "EMPLOYEE",
                "entityId", "employee:" + emp.id(),
                "department", emp.department(),
                "updatedAt", Instant.now().toString()
        );
        return new Document(buildEmployeeText(emp), metadata);
    }

    /* ==========================
       📁 PROJECT
       ========================== */
    private Document projectToDoc(ProjectRagDto p) {
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "PROJECT",
                "entityId", "project:" + p.code(),
                "client", p.client()
        );

        String content = """
                Proyecto de SmartHR.

                Nombre: %s.
                Código: %s.
                Cliente: %s.
                Ubicación: %s.
                Inicio: %s.
                %s
                """.formatted(
                p.name(),
                p.code(),
                p.client(),
                p.ubication(),
                p.startDate(),
                p.endDate() != null ? "Finalización: " + p.endDate() : "Proyecto activo"
        );

        return new Document(content, metadata);
    }

    /* ==========================
       🧠 SKILL
       ========================== */
    private Document skillToDoc(SkillRagDto s) {
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "SKILL",
                "entityId", "skill:" + s.name()
        );
        String content = """
                Habilidad en SmartHR.

                Nombre: %s.
                Descripción: %s.
                """.formatted(s.name(), s.description());
        return new Document(content, metadata);
    }

    /* ==========================
       🏢 DEPARTMENT
       ========================== */
    private Document departmentToDoc(DepartmentRagDto d) {
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "DEPARTMENT",
                "entityId", "department:" + d.name()
        );
        String content = """
                Departamento de SmartHR.

                Nombre: %s.
                Descripción: %s.
                """.formatted(d.name(), d.description());
        return new Document(content, metadata);
    }

    /* ==========================
       📝 LEAVE REQUEST
       ========================== */
    private Document leaveRequestToDoc(PendingLeaveRequestRagDto l) {
        String leaveId = l.employeeName() + ":" + l.startDate();
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "LEAVE_REQUEST",
                "entityId", "leave:" + leaveId,
                "leaveType", l.type()
        );
        String content = """
                Solicitud de ausencia.

                Empleado: %s.
                Tipo: %s.
                Periodo: %s → %s.
                Comentarios: %s.
                """.formatted(
                l.employeeName(),
                l.type(),
                l.startDate(),
                l.endDate(),
                l.comments() != null ? l.comments() : "Sin comentarios"
        );
        return new Document(content, metadata);
    }

    /* ==========================
       📄 EMPLOYEE TEXT
       ========================== */
    private String buildEmployeeText(EmployeeCompleteDto emp) {
        return """
                Empleado de SmartHR.

                Nombre: %s.
                Puesto: %s (%s).
                Ubicación: %s.
                Fecha de alta: %s.

                Habilidades: %s.
                Proyectos: %s.

                Contratro: %s.
                Jornada: %d horas/semana.
                Salario: %.2f € %s.
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
                emp.bonus() != null ? "(bonus " + emp.bonus() + " €)" : ""
        );
    }

    /* ==========================
       💬 CHAT RAG
       ========================== */
    public String chatWithRag(String message) {


        // 1️⃣ Respuesta directa a saludos
        if (message.matches("(?i)^(hola|buenos días|buenas|hello).*")) {
            return "Hola. Soy el asistente interno de SmartHR. ¿En qué puedo ayudarte?";
        }

        // 2️⃣ Búsqueda semántica
        List<Document> relevant = vectorStore.similaritySearch(message);

        if (relevant.isEmpty()) {
            return """
               No dispongo de información interna suficiente para responder a esa consulta.
               Para más detalles, contacte con el departamento de RRHH o con el administrador del sistema.
               """;
        }

        // 3️⃣ Limitamos resultados manualmente
        String context = relevant.stream()
                .limit(5)
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 4️⃣ Prompt profesional y controlado
        return chatClient.prompt()
                .system("""
                Eres SmartHR Assistant, el asistente corporativo interno de gestión de personas.

                Rol:
                - Asistes a empleados y managers con información interna de SmartHR
                - Respondes de forma profesional, clara y concisa
                - NO inventas información ni usas conocimiento externo
                - SOLO utilizas la información proporcionada en el contexto

                Estilo de respuesta:
                - Profesional y cordial
                - Sin emojis
                - Lenguaje corporativo
                - No saludes a menos que el usuario lo haga
                - No menciones entidades, personas o proyectos no presentes en el contexto

                Si la información no está disponible:
                - Indícalo claramente
                - Sugiere contactar con RRHH o administración

                Contexto interno SmartHR:
                -------------------------
                """ + context)
                .user(message)
                .call()
                .content();
    }


    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
