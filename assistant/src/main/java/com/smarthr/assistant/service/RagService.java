package com.smarthr.assistant.service;

import com.smarthr.assistant.dto.*;
import com.smarthr.assistant.utils.AssistantChatUtils;
import com.smarthr.assistant.utils.RagIntent;
import com.smarthr.assistant.utils.VgVectorInyection;
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
import com.smarthr.assistant.utils.VgVectorInyection.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private final ChatClient chatClient;

    @Autowired
    private final VectorStore vectorStore;

    @Autowired
    private final VgVectorInyection vgVectorInyection;

    @Autowired
    public final AssistantChatUtils assistantChatUtils;

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
                vgVectorInyection.upsertDocuments(documents, vectorStore);


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
        snapshot.employees().forEach(e -> docs.add(vgVectorInyection.employeeToDoc(e)));
        snapshot.projects().forEach(p -> docs.add(vgVectorInyection.projectToDoc(p)));
        snapshot.skills().forEach(s -> docs.add(vgVectorInyection.skillToDoc(s)));
        snapshot.departments().forEach(d -> docs.add(vgVectorInyection.departmentToDoc(d)));
        snapshot.pendingLeaveRequests().forEach(l -> docs.add(vgVectorInyection.leaveRequestToDoc(l)));
        return docs;
    }

    public List<Document> buildEmployee(EmployeeCompleteDto employee) {
        List<Document> docs = new ArrayList<>();
        docs.add(vgVectorInyection.employeeToDoc(employee)); // Solo el empleado
        return docs;
    }
    public String chatWithRag(String message) {

        RagIntent intent = detectIntent(message);
        System.out.println("🔍 Intent detected: " + intent);
        String enhancedQuery = assistantChatUtils.rewriteQuery(message);
        System.out.println("📝 Enhanced query: " + enhancedQuery);

        if (intent == RagIntent.LEAVE_REQUEST) {
            return handleAbsenceQuery(message);
        }

        if (intent == RagIntent.EMPLOYEE_BY_PROJECT) {
            return handleEmployeesByProject(message, enhancedQuery);
        }

        if (intent == RagIntent.PROJECT) {
            return handleProjects(message, enhancedQuery);
        }

        String employeeName = assistantChatUtils.extractEmployeeName(message);
        if (employeeName != null) {
            String cleanName = assistantChatUtils.normalize(employeeName);
            List<Document> employees = vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .topK(100) // aumentar topK
                                    .filterExpression("type == 'EMPLOYEE'")
                                    .build()
                    ).stream()
                    .filter(d -> assistantChatUtils.normalize(d.getText()).contains(cleanName) ||
                            assistantChatUtils.normalize((String)d.getMetadata().get("entityId")).contains(cleanName.replace(" ", "-")))
                    .toList();

            if (!employees.isEmpty()) {
                return answerWithContext(message, employees);
            } else {
                return """
            Lo siento, pero no tengo información sobre un empleado llamado %s en la empresa SmartHR.
            """.formatted(employeeName);
            }
        }

        String location = assistantChatUtils.extractEmployeeLocation(message);
        if (location != null) {
            List<Document> employees = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .topK(20)
                            .filterExpression(
                                    "type == 'EMPLOYEE' && location == '" + assistantChatUtils.capitalize(location) + "'"
                            )
                            .build()
            );

            if (!employees.isEmpty())
                return answerWithContext(message, employees);
        }

        if (assistantChatUtils.containsSkill(message)) {
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

        String department = assistantChatUtils.extractDepartment(message);
        String locationFallback = assistantChatUtils.extractEmployeeLocation(message);

        StringBuilder filterExpr = new StringBuilder("type == 'EMPLOYEE'");

        if (department != null) {
            filterExpr.append(" && department == '").append(department).append("'");
        }

        if (locationFallback != null) {
            filterExpr.append(" && location == '")
                    .append(assistantChatUtils.capitalize(locationFallback))
                    .append("'");
        }

        SearchRequest request = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(20)
                .similarityThreshold(0.2f)
                .filterExpression(filterExpr.toString())
                .build();

        List<Document> relevantDocs = vectorStore.similaritySearch(request);
        if (relevantDocs.isEmpty()) return assistantChatUtils.noDataResponse();

        return answerWithContext(message, relevantDocs);
    }

    public String chatForEmployee(String employeeName, String message) {
        String cleanName = assistantChatUtils.normalize(employeeName);
        log.info("👤 Chat for employee: {}", employeeName);

        RagIntent intent = detectIntent(message);
        log.info("🔍 Intent detected: {}", intent);

        if (intent == RagIntent.LEAVE_REQUEST) {
            return handleAbsenceQueryForEmployee(employeeName, message);
        }

        List<Document> employeeDocs = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .topK(20)
                                .filterExpression("type == 'EMPLOYEE'")
                                .build()
                ).stream()
                .filter(d -> assistantChatUtils.normalize(d.getText()).contains(cleanName))
                .toList();

        if (employeeDocs.isEmpty()) {
            return "No he encontrado información para " + employeeName;
        }

        return answerWithContext(message, employeeDocs);
    }

    public String handleAbsenceQueryForEmployee(String employeeName, String message) {

        String cleanName = assistantChatUtils.normalize(employeeName);

        List<Document> leaves = searchLeaveRequests(message).stream()
                .filter(doc -> assistantChatUtils.normalize(doc.getText()).contains(cleanName))
                .toList();

        if (leaves.isEmpty()) {
            return """
        No tienes solicitudes de ausencia registradas actualmente.
        [Fuente: Sistema de ausencias SmartHR]
        """;
        }

        String context = assistantChatUtils.buildContextWithMetadata(leaves);

        return chatClient.prompt()
                .system("""
        Eres SmartHR Assistant.

        REGLAS OBLIGATORIAS:
        - Responde ÚNICAMENTE con la información presente en el CONTEXTO.
        - NO resumas.
        - NO inventes fechas ni datos.
        - NO cambies los valores.
        - Si un dato no existe, escribe "No especificado".

        FORMATO DE SALIDA (exacto):
        ```
        Solicitudes de ausencia:
        • Tipo: <tipo>
          Periodo: <inicio> → <fin>
          Estado: <estado>
          Comentarios: <comentarios>
        [Fuente: Sistema de ausencias SmartHR]
        ```

        CONTEXTO:
        %s
        """.formatted(context))
                .user(message)
                .call()
                .content();

    }

    public String answerWithContext(String message, List<Document> docs) {
        String context = assistantChatUtils.buildContextWithMetadata(docs);

        return chatClient.prompt()
                .system("""
                Eres SmartHR Assistant.
                Responde SOLO con la información del contexto.
                """)
                .user(context + "\n\n" + message)
                .call()
                .content();
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

    public RagIntent detectIntent(String message) {
        String lower = assistantChatUtils.normalize(message);

        // 🔥 Caso especial: pregunta por proyectos de una persona
        if (assistantChatUtils.extractEmployeeName(message) != null
                && lower.contains("proyecto")) {
            return RagIntent.EMPLOYEE;
        }

        if (lower.contains("empleados") && lower.contains("proyecto"))
            return RagIntent.EMPLOYEE_BY_PROJECT;

        if (lower.contains("empleado") || lower.contains("empleados") || lower.contains("trabaja"))
            return RagIntent.EMPLOYEE;

        if (lower.contains("proyecto"))
            return RagIntent.PROJECT;

        if (lower.contains("departamento"))
            return RagIntent.DEPARTMENT;

        if (lower.contains("ausencia") || lower.contains("vacaciones") || lower.contains("permiso"))
            return RagIntent.LEAVE_REQUEST;

        return RagIntent.GENERIC;
    }


    public String handleProjects(String message, String enhancedQuery) {

        String targetProject = assistantChatUtils.extractProjectNameFromMessage(message);
        String targetLocation = assistantChatUtils.extractProjectLocation(message);
        String targetClient = assistantChatUtils.extractClientFromMessage(message);

        System.out.println("📝 target: " + targetLocation + ", " + targetProject + ", " + targetClient);

        //Buscar proyectos
        SearchRequest projectRequest = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(15)
                .similarityThreshold(0.2f)
                .filterExpression("type == 'PROJECT'")
                .build();

        List<Document> projectDocs = vectorStore.similaritySearch(projectRequest);

        // SIN FILTROS → todos los proyectos
        if (targetProject == null && targetLocation == null && targetClient == null) {
            return answerWithContext(message, projectDocs);
        }

        if (projectDocs.isEmpty()) return assistantChatUtils.noDataResponse();

        //Filtrar proyectos
        List<Document> matchedProjects = projectDocs.stream()
                .filter(doc -> {
                    Map<String, Object> meta = doc.getMetadata();

                    String pName = assistantChatUtils.normalize((String) meta.get("projectName"));
                    String pClient = assistantChatUtils.normalize((String) meta.get("client"));
                    String pLocation = assistantChatUtils.normalize((String) meta.get("ubication"));

                    boolean matchByName =
                            targetProject != null &&
                                    (pName.contains(targetProject) || targetProject.contains(pName));

                    boolean matchByClient =
                            targetClient != null &&
                                    pClient.contains(targetClient);

                    boolean matchByLocation =
                            targetLocation != null &&
                                    pLocation.equals(targetLocation);

                    return matchByName || matchByClient || matchByLocation;
                })
                .toList();

        if (matchedProjects.isEmpty()) return assistantChatUtils.noDataResponse();

        // ¿La pregunta pide EMPLEADOS?
        boolean wantsEmployees =
                assistantChatUtils.normalize(message).contains("empleado")
                        || assistantChatUtils.normalize(message).contains("trabajan")
                        || assistantChatUtils.normalize(message).contains("participan");

        if (!wantsEmployees) {
            return answerWithContext(message, matchedProjects);
        }

        // Buscar empleados
        List<Document> employeeDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .topK(100)
                        .filterExpression("type == 'EMPLOYEE'")
                        .build()
        );

        Set<String> projectNames = matchedProjects.stream()
                .map(d -> assistantChatUtils.normalize((String) d.getMetadata().get("projectName")))
                .collect(Collectors.toSet());

        // Filtrar empleados por proyectos
        List<Document> matchedEmployees = employeeDocs.stream()
                .filter(doc -> {
                    Object projectsMeta = doc.getMetadata().get("projects");

                    if (projectsMeta instanceof Collection<?> col) {
                        for (Object pObj : col) {
                            if (pObj instanceof Map<?, ?> pMap) {
                                String pName = assistantChatUtils.normalize((String) pMap.get("name"));
                                if (projectNames.stream()
                                        .anyMatch(n -> n.contains(pName) || pName.contains(n))) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                })
                .toList();

        if (matchedEmployees.isEmpty()) return assistantChatUtils.noDataResponse();

        return answerWithContext(message, matchedEmployees);
    }

    public String handleEmployeesByProject(String message, String enhancedQuery) {

        SearchRequest projectRequest = SearchRequest.builder()
                .query(enhancedQuery)
                .topK(10)
                .similarityThreshold(0.2f)
                .filterExpression("type == 'PROJECT'")
                .build();

        System.out.println("🔍 Searching projects with request: " + projectRequest);
        List<Document> projectDocs = vectorStore.similaritySearch(projectRequest);
        System.out.println("🔍 projectDocs: " + projectDocs.stream().map(Document::toString).collect(Collectors.joining("\n")));
        if (projectDocs.isEmpty()) return assistantChatUtils.noDataResponse();


        List<Document> employeeDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .topK(100)
                        .filterExpression("type == 'EMPLOYEE'")
                        .build()
        );

        String targetProject = assistantChatUtils.extractProjectNameFromMessage(message);
        System.out.println("🔍 targetProject: " + targetProject);
        String targetProjectLocation = assistantChatUtils.extractProjectLocation(message);
        System.out.println("🔍 targetProjectLocation: " + targetProjectLocation);

        Set<String> clientsNormalized = projectDocs.stream()
                .map(d ->  assistantChatUtils.normalize((String) d.getMetadata().get("client")))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        boolean hasProjectName = targetProject != null;
        boolean hasLocation = targetProjectLocation != null;
        boolean hasClient = !clientsNormalized.isEmpty();
        boolean hasProjectCode = assistantChatUtils.extractProjectCode(message) != null;

        if (!hasProjectName && !hasLocation && !hasClient && !hasProjectCode) {
            return assistantChatUtils.noDataResponse();
        }

        List<Document> matchedEmployees = employeeDocs.stream()
                .filter(doc -> {
                    Object projectsMeta = doc.getMetadata().get("projects");

                    if (projectsMeta instanceof Collection<?> col) {
                        for (Object pObj : col) {

                            if (pObj instanceof Map<?, ?> pMap) {

                                String pName = assistantChatUtils.normalize((String) pMap.get("name"));
                                String pClient = assistantChatUtils.normalize((String) pMap.get("client"));
                                String pLocation = assistantChatUtils.normalize((String) pMap.get("ubication"));

                                boolean matchByName = targetProject != null &&
                                        (pName.contains(targetProject) || targetProject.contains(pName));

                                boolean matchByClient = targetProject == null &&
                                        clientsNormalized.stream()
                                                .anyMatch(c -> c.contains(pClient) || pClient.contains(c));

                                boolean matchByLocation = targetProjectLocation != null &&
                                        pLocation.equals(targetProjectLocation);

                                if (matchByName || matchByClient || matchByLocation) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                })
                .toList();


        if (matchedEmployees.isEmpty()) return assistantChatUtils.noDataResponse();

        return answerWithContext(message, matchedEmployees);
    }

    public String handleAbsenceQuery(String message) {
        List<Document> leaves = searchLeaveRequests(message);

        String employeeName = assistantChatUtils.extractEmployeeName(message);

        if (employeeName != null) {
            String clean = assistantChatUtils.normalize(employeeName);
            leaves = leaves.stream().filter(doc -> assistantChatUtils.normalize(doc.getText()).contains(clean)).toList();
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

        String context = assistantChatUtils.buildContextWithMetadata(leaves);

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

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

}
