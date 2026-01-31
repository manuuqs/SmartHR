package com.smarthr.assistant.service;

import com.smarthr.assistant.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private static final double MIN_SCORE = 0.75;

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
                Proyecto interno de la empresa SmartHR llamado %s (código %s).
                Cliente: %s. Ubicación principal: %s.
                Inicio del proyecto: %s. %s.
                Este proyecto puede estar asociado a uno o varios empleados y departamentos de SmartHR.
                """
                .formatted(
                        p.name(),
                        p.code(),
                        p.client(),
                        p.ubication(),
                        p.startDate(),
                        p.endDate() != null ? "Fecha de finalización: " + p.endDate() : "Actualmente el proyecto sigue activo"
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
            Habilidad técnica utilizada en SmartHR: %s.
            Descripción de la habilidad: %s.
            Esta skill puede estar asociada a empleados que la usan en sus proyectos.
            """
                .formatted(s.name(), s.description());

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
            Departamento interno de SmartHR llamado %s.
            Descripción: %s.
            En este departamento trabajan varios empleados con diferentes puestos y habilidades.
            """
                        .formatted(d.name(), d.description());

        return new Document(content, metadata);
    }

    /* ==========================
       📝 LEAVE REQUEST
       ========================== */
    private Document leaveRequestToDoc(PendingLeaveRequestRagDto l) {
        String leaveId = l.employeeName() + ":" + l.startDate();
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "status", l.status(),
                "type", "LEAVE_REQUEST",
                "entityId", "leave:" + leaveId,
                "leaveType", l.type()
        );
        String content = """
                Solicitud de ausencia.

                Empleado: %s.
                Estado de la solicitud: %s.
                Tipo: %s.
                Periodo: %s → %s.
                Comentarios: %s.
                """.formatted(
                l.employeeName(),
                l.status(),
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
        String skills = emp.skills().isEmpty()
                ? "sin habilidades registradas explícitamente"
                : "con habilidades en " + String.join(", ", emp.skills());

        String projects = emp.projects().isEmpty()
                ? "sin proyectos asignados actualmente"
                : "participando en los proyectos " + String.join(", ", emp.projects());

        String bonus = emp.bonus() != null
                ? " y un bonus de " + emp.bonus() + " €"
                : "";

        return """
        Empleado de la empresa SmartHR llamado %s.
        Trabaja como %s en el departamento de %s, ubicado en %s, y se incorporó el %s.
        Es un perfil %s y actualmente está %s.
        Su contrato es de tipo %s, con una jornada de %d horas semanales y un salario base de %.2f €%s.
        """
                .formatted(
                        emp.name(),
                        emp.jobPosition(),
                        emp.department(),
                        emp.location(),
                        emp.hireDate(),
                        skills,
                        projects,
                        emp.contractType(),
                        emp.weeklyHours(),
                        emp.baseSalary(),
                        bonus
                );
    }


    /* ==========================
       💬 CHAT RAG
       ========================== */
    public String chatWithRag(String message) {

        String lower = message.toLowerCase();

        // 0️⃣ Caso especial: AUSENCIAS → saltamos al handler especializado
        if (lower.contains("ausencia") || lower.contains("ausencias")) {
            return handleAbsenceQuery(message);
        }

        // 1️⃣ Búsqueda semántica (TU MÉTODO EXISTENTE)
        String enhancedQuery = rewriteQuery(message);
        List<Document> relevant = vectorStore.similaritySearch(enhancedQuery);

        if (relevant.isEmpty()) {
            return noDataResponse();
        }

        // 2️⃣ FILTRO "no X" (Manuel, etc.)
        relevant = filterExcludeRequests(message, relevant);

        // 3️⃣ MIN_SCORE REDUCIDO + LOGS
        List<Document> highConfidence = relevant.stream()
                .filter(doc -> {
                    log.info("📊 Documento encontrado: {}", extractEntityName(doc.getText(), doc.getMetadata()));
                    return true; // 🔽 ACEPTA TODOS (era el problema MIN_SCORE)
                })
                .limit(5)
                .toList();

        if (highConfidence.isEmpty()) {
            String response = handleAbsenceFallback(message, relevant);
            if (!response.equals(noDataResponse())) {
                return response;
            }
            return noDataResponse();
        }

        // 4️⃣ Contexto con metadata
        String context = buildContextWithMetadata(highConfidence);

        // 5️⃣ Prompt PRO
        return chatClient.prompt()
                .system("""
    Eres SmartHR Assistant, asistente oficial de gestión de personas.

    REGLAS OBLIGATORIAS:
    1. SOLO datos del CONTEXTO (ignorar conocimiento externo)
    2. Español profesional, sin emojis en respuesta final
    3. Estructura: 1 oración + bullets + [Fuente]
    
    FORMATO EXACTO:
    ```
    Respuesta clara y directa.
    
    • Dato 1
    • Dato 2
    
    [Fuente: Nombre entidad]
    ```
    
    CONTEXTO VECTOR_STORE:
    %s
    """.formatted(context))
                .user("PREGUNTA: %s".formatted(message))
                .call()
                .content();
    }

    private List<Document> searchLeaveRequests(String message) {

        SearchRequest request = SearchRequest.builder()
                .query(" solicitud ausencia leave request sickness vacaciones baja médica ")
                .topK(20)
                .similarityThreshold(0.2f)
                .filterExpression("type == 'LEAVE_REQUEST'")
                .build();

        return vectorStore.similaritySearch(request);
    }

    public String handleAbsenceQuery(String message) {
        List<Document> leaves = searchLeaveRequests(message);

        log.info("🧪 LEAVE_REQUEST docs encontrados: {}", leaves.size());
        for (Document d : leaves) {
            log.info("📄 [{}] {}", d.getMetadata().get("entityId"), d.getText());
        }

        if (leaves.isEmpty()) {
            return """
            No hay solicitudes de ausencia registradas en el sistema.
            Para más detalles, consulte el módulo de ausencias de SmartHR.
            """;
        }

        String context = buildContextWithMetadata(leaves);

        return chatClient.prompt()
                .system("""
            Eres SmartHR Assistant, especializado en solicitudes de ausencia.
            A partir del contexto, responde qué solicitudes de ausencias hay.

            Formato:
            ```
            Resumen breve.
            • Empleado – Tipo – Periodo – Comentario
            [Fuente: Sistema de ausencias SmartHR]
            ```

            CONTEXTO:
            %s
            """.formatted(context))
                .user("Pregunta del usuario: %s".formatted(message))
                .call()
                .content();
    }


    // 🆕 QUERY REWRITING (30+ keywords clave)
    private String rewriteQuery(String original) {
        String lower = original.toLowerCase().trim();

        // 🔑 AUSENCIAS: solo enriquecemos la query, no llamamos al handler
        if (lower.contains("ausencia") || lower.contains("ausencias")) {
            return original + " solicitud ausencia leave request sickness pending approved vacaciones baja médica ";
        }

        if (lower.contains("pendiente") || lower.contains("pendientes")) {
            return original + " pending status abierto no aprobado solicitud";
        }

        // 💼 EMPLEADOS
        if (lower.contains("empleado") || lower.contains("empleados")) {
            return original + " nombre puesto departamento salario java spring boot desarrollo";
        }

        if (lower.contains("habilidad") || lower.contains("habilidades")) {
            return original + " java spring boot docker kubernetes postgresql redis git javascript";
        }

        if (lower.contains("salario") || lower.contains("sueldo")) {
            return original + " salario sueldo pago bonus contrato permanente precario";
        }

        return original;
    }


    // 🆕 FALLBACK AUSENCIAS (usa TU similaritySearch)
    private String handleAbsenceFallback(String message, List<Document> relevant) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("ausencia") || lowerMsg.contains("pendiente")) {
            // 🔧 Usa TU método existente
            List<Document> absenceDocs = vectorStore.similaritySearch(
                    "solicitud ausencia sickness leave request vacaciones baja");

            if (!absenceDocs.isEmpty()) {
                String context = buildContextWithMetadata(absenceDocs);
                return chatClient.prompt()
                        .system("""
        No hay ausencias PENDIENTES. Muestra HISTÓRICO disponible.
        
        FORMATO:
        "No hay ausencias pendientes. Histórico reciente:
        • Empleado X: Tipo Y (fecha)
        
        [Fuente: Sistema ausencias]"
        
        CONTEXTO: %s
        """.formatted(context))
                        .user(message)
                        .call()
                        .content();
            }
        }
        return noDataResponse();
    }

    // ✅ MÉTODOS SIMPLIFICADOS (sin dependencias raras)
    private List<Document> filterExcludeRequests(String message, List<Document> docs) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("no ") || lowerMsg.contains("excepto ")) {
            Pattern excludePattern = Pattern.compile("(?i)(no|excepto)\\s+([a-záéíóúñ]+(?:\\s+[a-záéíóúñ]+)?)");
            Matcher matcher = excludePattern.matcher(message);

            while (matcher.find()) {
                String excludeName = matcher.group(2).toLowerCase();
                docs.removeIf(doc -> doc.getText().toLowerCase().contains(excludeName));
            }
        }
        return docs;
    }

    private String buildContextWithMetadata(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    String text = doc.getText();
                    String entityName = extractEntityName(text);
                    return String.format("📄 %s\n%s\n", entityName, text);
                })
                .collect(Collectors.joining("\n---\n"));
    }

    private String extractEntityName(String text) {
        // 🔧 SIMPLIFICADO: solo regex básico
        Pattern p = Pattern.compile("(?i)nombre[:\\s]+([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(?:\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)?)");
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "Documento SmartHR";
    }


    private String extractEntityName(String text, Map<String, Object> metadata) {
        Pattern patterns[] = {
                Pattern.compile("(?i)nombre[:\\s]+([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(?:\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)?)"),
                Pattern.compile("(?i)(empleado|ausencia)[:\\s]+([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+(?:\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)?)")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) return m.group(1).trim();
        }
        return metadata.getOrDefault("type", "Documento").toString();
    }


    // =====================
    // Helpers
    // =====================

    private double getScore(Document doc) {
        Object score = doc.getMetadata().get("score");
        return (score instanceof Number n) ? n.doubleValue() : 1.0;
    }

    private String noDataResponse() {
        return """
        No dispongo de información interna suficiente para responder a esa consulta.
        Para más detalles, contacte con el departamento de Recursos Humanos
        o con el administrador del sistema.
        """;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

}
