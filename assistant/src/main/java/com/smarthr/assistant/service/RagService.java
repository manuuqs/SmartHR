package com.smarthr.assistant.service;

import com.smarthr.assistant.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
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
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

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

    private void upsertDocuments(List<Document> documents) {
        for (Document doc : documents) {
            try {
                String entityId = (String) doc.getMetadata().get("entityId");
                if (entityId != null) {
                    vectorStore.delete(List.of(entityId));
                }
            } catch (Exception ignored) {
            }
            vectorStore.add(List.of(doc));
        }
    }

    private List<Document> buildDocuments(CompanyRagSnapshotDto snapshot) {
        List<Document> docs = new ArrayList<>();
        snapshot.employees().forEach(e -> docs.add(employeeToDoc(e)));
        snapshot.projects().forEach(p -> docs.add(projectToDoc(p)));
        snapshot.skills().forEach(s -> docs.add(skillToDoc(s)));
        snapshot.departments().forEach(d -> docs.add(departmentToDoc(d)));
        snapshot.pendingLeaveRequests().forEach(l -> docs.add(leaveRequestToDoc(l)));
        return docs;
    }

    // ========================== EMPLEADO
    private Document employeeToDoc(EmployeeCompleteDto emp) {

        Map<String,Object> metadata = new HashMap<>();
        metadata.put("source", "smarthr");
        metadata.put("type", "EMPLOYEE");
        metadata.put("entityId", "employee:" + emp.id());
        metadata.put("jobPosition", emp.jobPosition());
        metadata.put("location", emp.location());
        metadata.put("department", emp.department());
        metadata.put("updatedAt", Instant.now().toString());

        // ✅ Proyectos completos del empleado
        List<ProjectRagDto> projects =
                emp.projectsInfo() != null ? emp.projectsInfo() : Collections.emptyList();


        if (!projects.isEmpty()) {
            metadata.put("projects", projects);
        }

        return new Document(buildEmployeeText(emp, projects), metadata);
    }


    private String buildEmployeeText(EmployeeCompleteDto emp, List<ProjectRagDto> projects) {

        String skills = emp.skills().isEmpty()
                ? "sin habilidades registradas explícitamente"
                : "con habilidades en " + String.join(", ", emp.skills());

        String projectsDetails = projects.isEmpty()
                ? "sin proyectos asignados actualmente"
                : projects.stream()
                .map(p -> String.format(
                        "%s (Código %s), Cliente: %s, Ubicación: %s, Inicio: %s, %s",
                        p.name(),
                        p.code(),
                        p.client(),
                        p.ubication(),
                        p.startDate(),
                        p.endDate() != null
                                ? "Fecha de finalización: " + p.endDate()
                                : "Actualmente activo"
                ))
                .collect(Collectors.joining("; "));

        String bonus = emp.bonus() != null
                ? " y un bonus de " + emp.bonus() + " €"
                : "";

        return """
    Empleado de la empresa SmartHR llamado %s.
    Trabaja como %s en el departamento de %s, ubicado en %s, y se incorporó el %s.
    Es un perfil %s y actualmente está participando en los proyectos: %s.
    Su contrato es de tipo %s, con una jornada de %d horas semanales y un salario base de %.2f €%s.
    """
                .formatted(
                        emp.name(),
                        emp.jobPosition(),
                        emp.department(),
                        emp.location(),
                        emp.hireDate(),
                        skills,
                        projectsDetails,
                        emp.contractType(),
                        emp.weeklyHours(),
                        emp.baseSalary(),
                        bonus
                );
    }


    private Document projectToDoc(ProjectRagDto p) {
        Map<String,Object> metadata = Map.of(
                "source", "smarthr",
                "type", "PROJECT",
                "entityId", "project:" + p.code(),
                "client", p.client(),
                "ubication", p.ubication()
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

    // ========================== CHAT RAG
    public String chatWithRag(String message) {

        RagIntent intent = detectIntent(message);
        String enhancedQuery = rewriteQuery(message);

        // ================== AUSENCIAS (NO TOCAR)
        if (intent == RagIntent.LEAVE_REQUEST) {
            return handleAbsenceQuery(message);
        }

        // ================== EMPLEADOS POR PROYECTO (NO TOCAR)
        if (intent == RagIntent.EMPLOYEE_BY_PROJECT) {
            return handleEmployeesByProject(message, enhancedQuery);
        }

        // ================== 🆕 EMPLEADO POR NOMBRE
        String employeeName = extractEmployeeName(message);
        if (employeeName != null) {
            String cleanName = normalize(employeeName);
            List<Document> employees = vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .topK(100) // aumentar topK
                                    .filterExpression("type == 'EMPLOYEE'")
                                    .build()
                    ).stream()
                    .filter(d -> normalize(d.getText()).contains(cleanName) ||
                            normalize((String)d.getMetadata().get("entityId")).contains(cleanName.replace(" ", "-")))
                    .toList();

            if (!employees.isEmpty()) {
                return answerWithContext(message, employees);
            } else {
                return """
            Lo siento, pero no tengo información sobre un empleado llamado %s en la empresa SmartHR.
            """.formatted(employeeName);
            }
        }


        // ================== 🆕 EMPLEADOS POR UBICACIÓN
        String location = extractEmployeeLocation(message);
        if (location != null) {
            List<Document> employees = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .topK(20)
                            .filterExpression(
                                    "type == 'EMPLOYEE' && location == '" + capitalize(location) + "'"
                            )
                            .build()
            );

            if (!employees.isEmpty())
                return answerWithContext(message, employees);
        }

        // ================== 🆕 EMPLEADOS POR SKILL
        if (containsSkill(message)) {
            List<Document> employees = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(enhancedQuery)
                            .topK(20)
                            .filterExpression("type == 'EMPLOYEE'")
                            .build()
            );

            if (!employees.isEmpty())
                return answerWithContext(message, employees);
        }

        // ================== FALLBACK GENERAL
        String department = extractDepartment(message);
        String locationFallback = extractEmployeeLocation(message);

        StringBuilder filterExpr = new StringBuilder("type == 'EMPLOYEE'");

        if (department != null) {
            filterExpr.append(" && department == '").append(department).append("'");
        }

        if (locationFallback != null) {
            filterExpr.append(" && location == '")
                    .append(capitalize(locationFallback))
                    .append("'");
        }

        SearchRequest request = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(20)
                .similarityThreshold(0.2f)
                .filterExpression(filterExpr.toString())
                .build();

        List<Document> relevantDocs = vectorStore.similaritySearch(request);
        if (relevantDocs.isEmpty()) return noDataResponse();

        return answerWithContext(message, relevantDocs);
    }

    private String extractEmployeeLocation(String message) {
        Pattern p = Pattern.compile("ubicaci[oó]n\\s+en\\s+([A-Za-zÁÉÍÓÚÑáéíóúñ ]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    private boolean containsSkill(String message) {
        String m = normalize(message);
        return m.contains("docker") || m.contains("kubernetes")
                || m.contains("java") || m.contains("spring")
                || m.contains("python");
    }

    private String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String answerWithContext(String message, List<Document> docs) {
        String context = buildContextWithMetadata(docs);

        return chatClient.prompt()
                .system("""
                Eres SmartHR Assistant.
                Responde SOLO con la información del contexto.
                """)
                .user(context + "\n\n" + message)
                .call()
                .content();
    }

    enum RagIntent {
        EMPLOYEE,
        PROJECT,
        DEPARTMENT,
        LEAVE_REQUEST,
        EMPLOYEE_BY_PROJECT,
        GENERIC
    }

    private List<Document> searchLeaveRequests(String message) {
        SearchRequest request = SearchRequest.builder()
                .query("solicitud ausencia vacaciones enfermedad baja excedencia leave request")
                .topK(30)
                .similarityThreshold(0.1f)
                .filterExpression("type == 'LEAVE_REQUEST'")
                .build();

        return vectorStore.similaritySearch(request);
    }

    private RagIntent detectIntent(String message) {
        String lower = normalize(message);

        if (lower.contains("empleados") && lower.contains("proyecto")) return RagIntent.EMPLOYEE_BY_PROJECT;
        if (lower.contains("empleado") || lower.contains("empleados") || lower.contains("trabaja")) return RagIntent.EMPLOYEE;
        if (lower.contains("proyecto")) return RagIntent.PROJECT;
        if (lower.contains("departamento")) return RagIntent.DEPARTMENT;
        if (lower.contains("ausencia") || lower.contains("vacaciones") || lower.contains("permiso")) return RagIntent.LEAVE_REQUEST;

        return RagIntent.GENERIC;
    }


    private String handleEmployeesByProject(String message, String enhancedQuery) {

        // 1️⃣ Buscar proyectos relevantes
        SearchRequest projectRequest = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(10)
                .similarityThreshold(0.2f)
                .filterExpression("type == 'PROJECT'")
                .build();

        List<Document> projectDocs = vectorStore.similaritySearch(projectRequest);
        if (projectDocs.isEmpty()) return noDataResponse();

        Set<String> projectNamesNormalized = projectDocs.stream()
                .map(d -> normalize(d.getText()))
                .collect(Collectors.toSet());

        Set<String> clientsNormalized = projectDocs.stream()
                .map(d -> normalize((String) d.getMetadata().get("client")))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        // 2️⃣ Traer empleados
        SearchRequest employeeRequest = SearchRequest.builder()
                .topK(100)
                .filterExpression("type == 'EMPLOYEE'")
                .build();

        List<Document> employeeDocs = vectorStore.similaritySearch(employeeRequest);

        // 3️⃣ Filtrar empleados por proyectos / clientes
        List<Document> matchedEmployees = employeeDocs.stream()
                .filter(doc -> {
                    Object projectsMeta = doc.getMetadata().get("projects");
                    if (projectsMeta instanceof Collection<?> col) {
                        for (Object pObj : col) {
                            if (pObj instanceof ProjectRagDto p) {
                                String pName = normalize(p.name());
                                String pClient = normalize(p.client());

                                // ✅ Coincidencia parcial por nombre o cliente
                                boolean matchProject = projectNamesNormalized.stream()
                                        .anyMatch(n -> n.contains(pName) || pName.contains(n));

                                boolean matchClient = clientsNormalized.isEmpty()
                                        || clientsNormalized.stream().anyMatch(c -> c.contains(pClient) || pClient.contains(c));

                                if (matchProject || matchClient) return true;
                            }
                        }
                    }
                    return false;
                })
                .toList();

        if (matchedEmployees.isEmpty()) return noDataResponse();

        return answerWithContext(message, matchedEmployees);
    }


    private String extractDepartment(String message) {
        String n = normalize(message);
        if (n.contains("desarrollo")) return "Desarrollo";
        if (n.contains("data")) return "Data";
        if (n.contains("marketing")) return "Marketing";
        if (n.contains("recursos humanos") || n.contains("rrhh")) return "Recursos Humanos";
        return null;
    }

    private String buildContextWithMetadata(List<Document> docs) {
        return docs.stream().map(d -> "📄 " + d.getText()).collect(Collectors.joining("\n---\n"));
    }

    private String noDataResponse() {
        return """
                No dispongo de información interna suficiente para responder a esa consulta.
                Contacte con Recursos Humanos.
                """;
    }

    public String handleAbsenceQuery(String message) {
        List<Document> leaves = searchLeaveRequests(message);

        String employeeName = extractEmployeeName(message);

        if (employeeName != null) {
            String clean = normalize(employeeName);
            leaves = leaves.stream().filter(doc -> normalize(doc.getText()).contains(clean)).toList();
        }

        if (leaves.isEmpty() && employeeName != null) {
            leaves = searchLeaveRequests("ausencias historicas");
        }

        if (leaves.isEmpty()) {
            return """
        No hay solicitudes de ausencia registradas en el sistema.
        [Fuente: Sistema de ausencias SmartHR]
        """;
        }

        String context = buildContextWithMetadata(leaves);

        return chatClient.prompt()
                .system("""
        Eres SmartHR Assistant, especializado en solicitudes de ausencia.
        A partir del contexto, responde qué solicitudes de ausencias existen.

        Formato:
        ```
        Resumen breve.
        • Empleado – Tipo – Periodo – Comentario
        [Fuente: Sistema de ausencias SmartHR]
        ```

        CONTEXTO:
        %s
        """.formatted(context))
                .user(message)
                .call()
                .content();
    }

    private String extractEmployeeName(String message) {
        Pattern p = Pattern.compile("([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1);
        return null;
    }

    private String rewriteQuery(String original) {
        String lower = original.toLowerCase().trim();
        if (lower.contains("ausencia") || lower.contains("ausencias"))
            return original + " solicitud ausencia leave request sickness pending approved vacaciones baja médica ";
        if (lower.contains("pendiente") || lower.contains("pendientes"))
            return original + " pending status abierto no aprobado solicitud";
        if (lower.contains("empleado") || lower.contains("empleados"))
            return original + " empleado nombre departamento puesto";
        if (lower.contains("habilidad") || lower.contains("habilidades"))
            return original + " java spring boot docker kubernetes postgresql redis git javascript";
        if (lower.contains("salario") || lower.contains("sueldo"))
            return original + " salario sueldo pago bonus contrato permanente precario";
        return original;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

}
