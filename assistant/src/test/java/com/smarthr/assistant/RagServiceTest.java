package com.smarthr.assistant;

import com.smarthr.assistant.dto.EmployeeCompleteDto;
import com.smarthr.assistant.dto.CompanyRagSnapshotDto;
import com.smarthr.assistant.service.RagService;
import com.smarthr.assistant.utils.AssistantChatUtils;
import com.smarthr.assistant.utils.RagIntent;
import com.smarthr.assistant.utils.VgVectorInyection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestTemplate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RagServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private VgVectorInyection vgVectorInyection;

    @Mock
    private AssistantChatUtils assistantChatUtils;

    @InjectMocks
    private RagService ragService;

    @BeforeEach
    void setup() {
        assertNotNull(ragService);
    }

    @Test
    void buildEmployee_shouldReturnOneDocument() {
        EmployeeCompleteDto employee = mock(EmployeeCompleteDto.class);
        Document doc = new Document("Empleado Ana García", Map.of("type", "EMPLOYEE"));
        when(vgVectorInyection.employeeToDoc(employee)).thenReturn(doc);

        List<Document> result = ragService.buildEmployee(employee);

        assertEquals(1, result.size());
        assertEquals("Empleado Ana García", result.get(0).getText());
        verify(vgVectorInyection).employeeToDoc(employee);
    }

    @Test
    void chatWithRag_employeeNotFound_returnsNoData() {
        String message = "Empleado que no existe";

        when(assistantChatUtils.normalize(anyString())).thenReturn(message.toLowerCase());
        when(assistantChatUtils.extractEmployeeName(message)).thenReturn("NoExiste");
        when(assistantChatUtils.rewriteQuery(message)).thenReturn(message); // no null
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of()); // vectorStore sí se usa

        String response = ragService.chatWithRag(message);

        assertEquals(
                "Lo siento, pero no tengo información sobre un empleado llamado NoExiste en la empresa SmartHR.",
                response.trim()
        );
    }

    @Test
    void chatWithRag_employeeFound_returnsAnswer() {
        String message = "Info Ana";
        Document doc = new Document("Ana", Map.of("type", "EMPLOYEE", "entityId", "ana"));

        when(assistantChatUtils.normalize(anyString())).thenReturn("ana");
        when(assistantChatUtils.extractEmployeeName(message)).thenReturn("Ana");
        when(assistantChatUtils.rewriteQuery(message)).thenReturn(message);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(assistantChatUtils.buildContextWithMetadata(List.of(doc))).thenReturn("contexto");

        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("respuesta");

        String response = ragService.chatWithRag(message);
        assertEquals("respuesta", response);
    }

    @Test
    void chatForEmployee_noEmployeeDocs_returnsNotFoundMessage() {
        String message = "Info Ana";
        String employeeName = "Ana";

        // Mockear normalize tanto para el nombre como para el mensaje
        when(assistantChatUtils.normalize(employeeName)).thenReturn("ana");
        when(assistantChatUtils.normalize(message)).thenReturn("info ana"); // <-- clave
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        String response = ragService.chatForEmployee(employeeName, message);
        assertEquals("No he encontrado información para Ana", response);
    }

    @Test
    void chatForEmployee_employeeDocs_returnsAnswer() {
        String message = "Info Ana";
        String employeeName = "Ana";
        Document doc = new Document("Ana", Map.of("type", "EMPLOYEE"));

        // Normalizar empleado y mensaje
        when(assistantChatUtils.normalize(employeeName)).thenReturn("ana");
        when(assistantChatUtils.normalize(message)).thenReturn("info ana");

        // Mockear vectorStore con un doc
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc));

        when(assistantChatUtils.buildContextWithMetadata(List.of(doc)))
                .thenReturn("contexto");

        // Mockear el flujo de prompt() con Spring AI API real
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn("respuesta");

        String response = ragService.chatForEmployee(employeeName, message);

        assertEquals("respuesta", response);
    }

    @Test
    void handleAbsenceQuery_noLeaves_returnsNoData() {
        String message = "Consulta ausencia";

        // El método extrae el nombre del empleado
        when(assistantChatUtils.extractEmployeeName(message)).thenReturn(null);

        // La búsqueda en vectorStore devuelve vacío
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // Ejecutar
        String response = ragService.handleAbsenceQuery(message);

        // Verificar que devuelva el mensaje esperado
        assertTrue(response.contains("No hay solicitudes"));
    }

    @Test
    void handleAbsenceQueryForEmployee_noLeaves_returnsNoLeavesMessage() {
        String message = "Consulta ausencia";
        String employeeName = "Ana";

        when(assistantChatUtils.normalize(employeeName)).thenReturn("ana");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        String response = ragService.handleAbsenceQueryForEmployee(employeeName, message);
        assertTrue(response.contains("No tienes solicitudes"));
    }

    @Test
    void syncSmartHRData_callsVectorUpsert() {
        CompanyRagSnapshotDto snapshot = mock(CompanyRagSnapshotDto.class);
        when(restTemplate.exchange(anyString(), any(), any(), eq(CompanyRagSnapshotDto.class)))
                .thenReturn(new org.springframework.http.ResponseEntity<>(snapshot, org.springframework.http.HttpStatus.OK));
        when(snapshot.employees()).thenReturn(List.of());
        when(snapshot.projects()).thenReturn(List.of());
        when(snapshot.skills()).thenReturn(List.of());
        when(snapshot.departments()).thenReturn(List.of());
        when(snapshot.pendingLeaveRequests()).thenReturn(List.of());

        ragService.syncSmartHRData();

        verify(vgVectorInyection).upsertDocuments(anyList(), eq(vectorStore));
    }

    @Test
    void chatWithRag_leaveRequest_callsHandleAbsenceQuery() {
        String message = "Consulta ausencia";

        // Creamos un spy del servicio para poder stubear métodos internos
        RagService spyService = spy(ragService);

        // Stubear detectIntent para forzar la rama LEAVE_REQUEST
        doReturn(RagIntent.LEAVE_REQUEST).when(spyService).detectIntent(message);

        // Stubear handleAbsenceQuery para no ejecutar la lógica real
        doReturn("No hay datos").when(spyService).handleAbsenceQuery(message);

        String response = spyService.chatWithRag(message);

        assertEquals("No hay datos", response);
        verify(spyService).handleAbsenceQuery(message);
    }


    @Test
    void chatWithRag_employeeByProject_callsHandleEmployeesByProject() {
        String message = "Mostrar empleados por proyecto";

        RagService spyService = spy(ragService);

        when(assistantChatUtils.rewriteQuery(message)).thenReturn(message);

        doReturn(RagIntent.EMPLOYEE_BY_PROJECT).when(spyService).detectIntent(message);
        doReturn("resultado proyecto").when(spyService).handleEmployeesByProject(message, message);

        String response = spyService.chatWithRag(message);

        assertEquals("resultado proyecto", response);
        verify(spyService).handleEmployeesByProject(message, message);
    }


    @Test
    void chatWithRag_projectIntent_callsHandleProjects() {
        String message = "Info de proyectos";

        lenient().when(assistantChatUtils.normalize(message)).thenReturn(message.toLowerCase());
        when(assistantChatUtils.rewriteQuery(message)).thenReturn(message);
        lenient().when(assistantChatUtils.extractEmployeeName(message)).thenReturn(null);
        lenient().when(assistantChatUtils.rewriteQuery(message)).thenReturn(message); // enhancedQuery no puede ser null

        RagService spyService = spy(ragService);

        doReturn(RagIntent.PROJECT).when(spyService).detectIntent(message);

        doReturn("resultado proyectos").when(spyService).handleProjects(message, message);

        String response = spyService.chatWithRag(message);

        assertEquals("resultado proyectos", response);
        verify(spyService).handleProjects(message, message);
    }

    @Test
    void chatWithRag_locationFilters_callsAnswerWithContext() {
        String message = "Buscar empleados en Madrid";
        String location = "Madrid";

        when(assistantChatUtils.normalize(anyString())).thenReturn(message.toLowerCase());
        when(assistantChatUtils.extractEmployeeName(message)).thenReturn(null);
        when(assistantChatUtils.extractEmployeeLocation(message)).thenReturn(location);
        when(assistantChatUtils.capitalize(location)).thenReturn(location);

        Document doc = new Document("Empleado1", Map.of("type", "EMPLOYEE"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        RagService spyService = spy(ragService);
        doReturn("respuesta con contexto").when(spyService).answerWithContext(message, List.of(doc));

        String response = spyService.chatWithRag(message);

        assertEquals("respuesta con contexto", response);
        verify(spyService).answerWithContext(message, List.of(doc));
    }

    @Test
    void chatWithRag_containsSkill_callsAnswerWithContext() {
        String message = "Empleado con Java";

        // Lenient stubs para evitar UnnecessaryStubbingException
        lenient().when(assistantChatUtils.normalize(anyString())).thenReturn(message.toLowerCase());
        lenient().when(assistantChatUtils.extractEmployeeName(message)).thenReturn(null);
        lenient().when(assistantChatUtils.extractEmployeeLocation(message)).thenReturn(null);
        lenient().when(assistantChatUtils.containsSkill(message)).thenReturn(true);
        lenient().when(assistantChatUtils.rewriteQuery(message)).thenReturn(message); // clave

        Document doc = new Document("Empleado2", Map.of("type", "EMPLOYEE"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        RagService spyService = spy(ragService);
        doReturn("respuesta skill").when(spyService).answerWithContext(message, List.of(doc));

        String response = spyService.chatWithRag(message);

        assertEquals("respuesta skill", response);
        verify(spyService).answerWithContext(message, List.of(doc));
    }

    @Test
    void chatWithRag_departmentAndLocationFilters_callsAnswerWithContext() {
        String message = "Empleado de IT en Barcelona";

        lenient().when(assistantChatUtils.extractDepartment(message)).thenReturn("IT");

        lenient().when(assistantChatUtils.normalize(anyString())).thenReturn(message.toLowerCase());
        lenient().when(assistantChatUtils.extractEmployeeName(message)).thenReturn(null);
        lenient().when(assistantChatUtils.extractEmployeeLocation(message)).thenReturn("Barcelona");
        lenient().when(assistantChatUtils.extractDepartment(message)).thenReturn("IT");
        lenient().when(assistantChatUtils.capitalize("Barcelona")).thenReturn("Barcelona");
        Document doc = new Document("Empleado3", Map.of("type", "EMPLOYEE"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        RagService spyService = spy(ragService);
        doReturn("respuesta dept").when(spyService).answerWithContext(message, List.of(doc));

        String response = spyService.chatWithRag(message);

        assertEquals("respuesta dept", response);
        verify(spyService).answerWithContext(message, List.of(doc));
    }


    @Test
    void chatWithRag_departmentAndLocationFallback_returnsAnswerWithContext() {
        String message = "Buscar empleados de IT en Madrid";
        String department = "IT";
        String locationFallback = "Madrid";
        String enhancedQuery = message;

        RagService spyService = spy(ragService);

        doReturn(message.toLowerCase()).when(assistantChatUtils).normalize(anyString());

        doReturn(null).when(assistantChatUtils).extractEmployeeName(message);

        doReturn(locationFallback).when(assistantChatUtils).extractEmployeeLocation(message);

        doReturn(false).when(assistantChatUtils).containsSkill(message);

        doReturn(department).when(assistantChatUtils).extractDepartment(message);

        doReturn(enhancedQuery).when(assistantChatUtils).rewriteQuery(message);

        doReturn(locationFallback).when(assistantChatUtils).capitalize(locationFallback);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "EMPLOYEE");
        metadata.put("department", department);
        metadata.put("location", locationFallback);
        Document finalDoc = new Document("EmpleadoTest", metadata);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(finalDoc));


        doReturn("respuesta dept+loc")
                .when(spyService)
                .answerWithContext(anyString(), anyList());


        String response = spyService.chatWithRag(message);


        assertEquals("respuesta dept+loc", response);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore, atLeast(2)).similaritySearch(captor.capture());

        List<SearchRequest> allRequests = captor.getAllValues();

        System.out.println("\n📊 Total de búsquedas capturadas: " + allRequests.size());
        for (int i = 0; i < allRequests.size(); i++) {
            System.out.println("🔍 Búsqueda " + (i+1) + ": " + allRequests.get(i).getFilterExpression());
        }

        SearchRequest lastRequest = allRequests.get(allRequests.size() - 1);
        String lastFilter = lastRequest.getFilterExpression().toString();

        boolean hasType = lastFilter.contains("type") && lastFilter.contains("EMPLOYEE");
        boolean hasDept = lastFilter.contains("department") && lastFilter.contains(department);
        boolean hasLoc = lastFilter.contains("location") && lastFilter.contains(locationFallback);

        String errorMsg = String.format(
                "La última búsqueda debe contener type, department y location.\n" +
                        "Filtro capturado: %s\n" +
                        "¿Tiene type? %s\n" +
                        "¿Tiene department? %s\n" +
                        "¿Tiene location? %s",
                lastFilter, hasType, hasDept, hasLoc
        );

        assertTrue(hasType && hasDept && hasLoc, errorMsg);
    }


    @Test
    @DisplayName("handleProjects: Búsqueda simple sin filtros - devuelve todos los proyectos")
    void handleProjects_noFilters_returnsAllProjects() {
        String message = "Muéstrame los proyectos";
        String enhancedQuery = message;

        // NO extraer filtros
        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(null);

        // Crear proyectos mock
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("projectName", "Portal Web");
        meta1.put("client", "Nike");
        meta1.put("ubication", "Madrid");
        Document project1 = new Document("Proyecto Portal Web", meta1);

        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("projectName", "App Mobile");
        meta2.put("client", "IBM");
        meta2.put("ubication", "Barcelona");
        Document project2 = new Document("Proyecto App Mobile", meta2);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(project1, project2));

        RagService spyService = spy(ragService);
        doReturn("Lista de proyectos").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleProjects(message, enhancedQuery);

        assertEquals("Lista de proyectos", response);
        verify(spyService).answerWithContext(eq(message), argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("handleProjects: Filtrar por nombre de proyecto")
    void handleProjects_filterByProjectName_returnsMatchedProjects() {
        String message = "Proyectos del Portal Web";
        String enhancedQuery = message;
        String targetProject = "portal web";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(targetProject);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(null);

        // ✅ CORRECCIÓN: Usar anyString() con un Answer que normalice cualquier entrada
        when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyecto que coincide
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("projectName", "Portal Web Corporativo");
        Document project1 = new Document("Portal Web Corporativo", meta1);

        // Proyecto que NO coincide
        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("projectName", "App Mobile");
        Document project2 = new Document("App Mobile", meta2);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(project1, project2));

        RagService spyService = spy(ragService);
        doReturn("Proyecto Portal Web").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleProjects(message, enhancedQuery);

        assertEquals("Proyecto Portal Web", response);
        verify(spyService).answerWithContext(eq(message), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getMetadata().get("projectName").equals("Portal Web Corporativo")
        ));
    }

    @Test
    @DisplayName("handleProjects: Filtrar por cliente")
    void handleProjects_filterByClient_returnsMatchedProjects() {
        String message = "Proyectos del cliente Nike";
        String enhancedQuery = message;
        String targetClient = "nike";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(targetClient);

        // ✅ CORRECCIÓN: Usar anyString() en lugar de mockear valores específicos
        when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyecto de Nike
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("projectName", "Portal Web");
        meta1.put("client", "Nike");
        Document project1 = new Document("Portal Nike", meta1);

        // Proyecto de IBM
        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("projectName", "App Mobile");
        meta2.put("client", "IBM");
        Document project2 = new Document("App IBM", meta2);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(project1, project2));

        RagService spyService = spy(ragService);
        doReturn("Proyectos Nike").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleProjects(message, enhancedQuery);

        assertEquals("Proyectos Nike", response);
        verify(spyService).answerWithContext(eq(message), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getMetadata().get("client").equals("Nike")
        ));
    }

    @Test
    @DisplayName("handleProjects: Filtrar por ubicación")
    void handleProjects_filterByLocation_returnsMatchedProjects() {
        String message = "Proyectos en Madrid";
        String enhancedQuery = message;
        String targetLocation = "madrid";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(targetLocation);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(null);

        // ✅ CORRECCIÓN: Usar lenient() para evitar UnnecessaryStubbingException
        lenient().when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyecto en Madrid
        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("projectName", "Portal Web");
        meta1.put("ubication", "Madrid");
        Document project1 = new Document("Portal Madrid", meta1);

        // Proyecto en Barcelona
        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("projectName", "App Mobile");
        meta2.put("ubication", "Barcelona");
        Document project2 = new Document("App Barcelona", meta2);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(project1, project2));

        RagService spyService = spy(ragService);
        doReturn("Proyectos Madrid").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleProjects(message, enhancedQuery);

        assertEquals("Proyectos Madrid", response);
        verify(spyService).answerWithContext(eq(message), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getMetadata().get("ubication").equals("Madrid")
        ));
    }

    @Test
    @DisplayName("handleProjects: Sin proyectos encontrados - devuelve noDataResponse")
    void handleProjects_noProjectsFound_returnsNoData() {
        String message = "Proyectos de cliente inexistente";
        String enhancedQuery = message;

        // ✅ CAMBIO: Proporcionar al menos UN filtro para que el código busque
        String targetClient = "clienteinexistente";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(targetClient); // ✅ Proporcionar filtro

        lenient().when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // No hay proyectos (la búsqueda devuelve vacío)
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        when(assistantChatUtils.noDataResponse()).thenReturn("No hay datos");

        String response = ragService.handleProjects(message, enhancedQuery);

        assertEquals("No hay datos", response);
        verify(assistantChatUtils).noDataResponse();
    }

    @Test
    @DisplayName("handleProjects: Pregunta pide empleados - busca empleados del proyecto")
    void handleProjects_questionAsksForEmployees_searchesEmployees() {
        String message = "¿Quiénes trabajan en el Portal Web?";
        String enhancedQuery = message;
        String targetProject = "portal web";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(targetProject);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyecto encontrado
        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Portal Web Corporativo");
        Document project = new Document("Portal Web", projectMeta);

        // Empleados encontrados
        Map<String, Object> empMeta1 = new HashMap<>();
        empMeta1.put("name", "Ana García");
        List<Map<String, Object>> projects1 = new ArrayList<>();
        Map<String, Object> proj1 = new HashMap<>();
        proj1.put("name", "Portal Web Corporativo");
        projects1.add(proj1);
        empMeta1.put("projects", projects1);
        Document emp1 = new Document("Ana García", empMeta1);

        // Mock de búsquedas múltiples
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.singletonList(project))  // 1ª búsqueda: proyectos
                .thenReturn(Collections.singletonList(emp1));    // 2ª búsqueda: empleados

        RagService spyService = spy(ragService);
        doReturn("Empleados del proyecto").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleProjects(message, enhancedQuery);

        assertEquals("Empleados del proyecto", response);

        // Verificar que hizo 2 búsquedas: proyectos + empleados
        verify(vectorStore, times(2)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("handleProjects: Pide empleados pero no hay empleados - devuelve noData")
    void handleProjects_asksEmployeesButNoneFound_returnsNoData() {
        String message = "¿Quiénes trabajan en proyecto inexistente?";
        String enhancedQuery = message;
        String targetProject = "proyecto inexistente";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(targetProject);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractClientFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyecto encontrado
        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Proyecto Test");
        Document project = new Document("Proyecto", projectMeta);

        // Mock de búsquedas: proyecto encontrado, pero sin empleados
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.singletonList(project))  // 1ª: proyectos
                .thenReturn(Collections.emptyList());            // 2ª: empleados (vacío)

        when(assistantChatUtils.noDataResponse()).thenReturn("No hay empleados");

        String response = ragService.handleProjects(message, enhancedQuery);

        assertEquals("No hay empleados", response);
        verify(assistantChatUtils).noDataResponse();
    }

    @Test
    @DisplayName("handleEmployeesByProject: Búsqueda básica por nombre de proyecto")
    void handleEmployeesByProject_byProjectName_returnsEmployees() {
        String message = "Empleados del Portal Web";
        String enhancedQuery = message;
        String targetProject = "portal web";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(targetProject);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn("");
        when(assistantChatUtils.extractProjectCode(message)).thenReturn("");

        lenient().when(assistantChatUtils.normalize(any())).thenAnswer(i -> {
            Object arg = i.getArgument(0);
            if (arg == null || arg.toString().isEmpty()) return "";
            return arg.toString().toLowerCase();
        });

        // Proyecto
        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Portal Web Corporativo");
        Document project = new Document("Portal Web", projectMeta);

        Map<String, Object> empMeta1 = new HashMap<>();
        List<Map<String, Object>> projects1 = new ArrayList<>();
        Map<String, Object> proj1 = new HashMap<>();
        proj1.put("name", "Portal Web Corporativo");  // El código busca por "name"
        proj1.put("projectName", "Portal Web Corporativo");  // Por si acaso
        projects1.add(proj1);
        empMeta1.put("projects", projects1);
        empMeta1.put("type", "EMPLOYEE");
        Document emp1 = new Document("Ana García", empMeta1);

        Map<String, Object> empMeta2 = new HashMap<>();
        List<Map<String, Object>> projects2 = new ArrayList<>();
        Map<String, Object> proj2 = new HashMap<>();
        proj2.put("name", "Otro Proyecto");
        proj2.put("projectName", "Otro Proyecto");
        projects2.add(proj2);
        empMeta2.put("projects", projects2);
        empMeta2.put("type", "EMPLOYEE");
        Document emp2 = new Document("Carlos López", empMeta2);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenAnswer(invocation -> {
                    SearchRequest req = invocation.getArgument(0);
                    String filter = req.getFilterExpression().toString();

                    // Primera búsqueda: proyectos
                    if (filter.contains("PROJECT")) {
                        return Collections.singletonList(project);
                    }
                    // Segunda búsqueda: empleados
                    else if (filter.contains("EMPLOYEE")) {
                        return Arrays.asList(emp1, emp2);
                    }
                    return Collections.emptyList();
                });

        RagService spyService = spy(ragService);
        doReturn("Empleados encontrados").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleEmployeesByProject(message, enhancedQuery);

        assertEquals("Empleados encontrados", response);


    }

    @Test
    @DisplayName("handleEmployeesByProject: Filtrar por cliente del proyecto")
    void handleEmployeesByProject_byClient_returnsEmployees() {
        String message = "Empleados de proyectos de Nike";
        String enhancedQuery = message;
        String targetClient = "nike";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(null);
        when(assistantChatUtils.extractProjectCode(message)).thenReturn(null);
        when(assistantChatUtils.normalize(anyString())).thenAnswer(i ->
                ((String) i.getArgument(0)).toLowerCase()
        );

        // Proyectos
        Map<String, Object> projectMeta1 = new HashMap<>();
        projectMeta1.put("projectName", "Portal Web");
        projectMeta1.put("client", "Nike");
        Document project1 = new Document("Portal Nike", projectMeta1);

        Map<String, Object> projectMeta2 = new HashMap<>();
        projectMeta2.put("projectName", "App Mobile");
        projectMeta2.put("client", "IBM");
        Document project2 = new Document("App IBM", projectMeta2);

        // Empleado en proyecto Nike
        Map<String, Object> empMeta = new HashMap<>();
        List<Map<String, Object>> projects = new ArrayList<>();
        Map<String, Object> proj = new HashMap<>();
        proj.put("name", "Portal Web");
        proj.put("client", "Nike");
        projects.add(proj);
        empMeta.put("projects", projects);
        Document emp = new Document("Ana García", empMeta);

        // Mock búsquedas
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(project1, project2))   // 1ª: proyectos
                .thenReturn(Collections.singletonList(emp));     // 2ª: empleados

        RagService spyService = spy(ragService);
        doReturn("Empleados Nike").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleEmployeesByProject(message, enhancedQuery);

        assertEquals("Empleados Nike", response);
        verify(spyService).answerWithContext(eq(message), anyList());
    }

    @Test
    @DisplayName("handleEmployeesByProject: Filtrar por ubicación del proyecto")
    void handleEmployeesByProject_byLocation_returnsEmployees() {
        String message = "Empleados en proyectos de Madrid";
        String enhancedQuery = message;
        String targetLocation = "madrid";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn("");
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn(targetLocation);
        when(assistantChatUtils.extractProjectCode(message)).thenReturn("");

        lenient().when(assistantChatUtils.normalize(any())).thenAnswer(i -> {
            Object arg = i.getArgument(0);
            if (arg == null || arg.toString().isEmpty()) return "";
            return arg.toString().toLowerCase();
        });

        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Portal Web");
        projectMeta.put("ubication", "Madrid");
        Document project = new Document("Portal Madrid", projectMeta);

        Map<String, Object> empMeta = new HashMap<>();
        List<Map<String, Object>> projects = new ArrayList<>();
        Map<String, Object> proj = new HashMap<>();
        proj.put("name", "Portal Web");
        proj.put("projectName", "Portal Web");  // Por si acaso
        proj.put("ubication", "Madrid");  // ✅ Incluir ubicación en el proyecto del empleado
        projects.add(proj);
        empMeta.put("projects", projects);
        empMeta.put("type", "EMPLOYEE");
        Document emp = new Document("Ana García", empMeta);

        // ✅ Mock inteligente que distingue entre búsquedas
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenAnswer(invocation -> {
                    SearchRequest req = invocation.getArgument(0);
                    String filter = req.getFilterExpression().toString();

                    // Primera búsqueda: proyectos
                    if (filter.contains("PROJECT")) {
                        return Collections.singletonList(project);
                    }
                    // Segunda búsqueda: empleados
                    else if (filter.contains("EMPLOYEE")) {
                        return Collections.singletonList(emp);
                    }
                    return Collections.emptyList();
                });

        RagService spyService = spy(ragService);
        doReturn("Empleados Madrid").when(spyService).answerWithContext(eq(message), anyList());

        String response = spyService.handleEmployeesByProject(message, enhancedQuery);

        assertEquals("Empleados Madrid", response);
        verify(spyService).answerWithContext(eq(message), anyList());
    }

    @Test
    @DisplayName("handleEmployeesByProject: Sin filtros - devuelve noData")
    void handleEmployeesByProject_noFilters_returnsNoData() {
        String message = "Empleados de proyectos";
        String enhancedQuery = message;

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn("");
        lenient().when(assistantChatUtils.extractProjectLocation(message)).thenReturn("");
        lenient().when(assistantChatUtils.extractProjectCode(message)).thenReturn("");

        lenient().when(assistantChatUtils.normalize(any())).thenAnswer(i -> {
            Object arg = i.getArgument(0);
            if (arg == null || arg.toString().isEmpty()) return "";
            return arg.toString().toLowerCase();
        });

        // Hay proyectos pero sin filtros aplicables
        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Portal Web");
        Document project = new Document("Portal", projectMeta);

        lenient().when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.singletonList(project));

        when(assistantChatUtils.noDataResponse()).thenReturn("Faltan filtros");

        String response = ragService.handleEmployeesByProject(message, enhancedQuery);

        assertEquals("Faltan filtros", response);
        verify(assistantChatUtils).noDataResponse();
    }

    @Test
    @DisplayName("handleEmployeesByProject: Empleado con metadata.projects null - no falla")
    void handleEmployeesByProject_employeeWithNullProjects_doesNotFail() {
        String message = "Empleados del Portal Web";
        String enhancedQuery = message;
        String targetProject = "portal web";

        when(assistantChatUtils.extractProjectNameFromMessage(message)).thenReturn(targetProject);
        when(assistantChatUtils.extractProjectLocation(message)).thenReturn("");
        when(assistantChatUtils.extractProjectCode(message)).thenReturn("");

        lenient().when(assistantChatUtils.normalize(any())).thenAnswer(i -> {
            Object arg = i.getArgument(0);
            if (arg == null || arg.toString().isEmpty()) return "";
            return arg.toString().toLowerCase();
        });

        Map<String, Object> projectMeta = new HashMap<>();
        projectMeta.put("projectName", "Portal Web");
        Document project = new Document("Portal", projectMeta);

        Map<String, Object> empMeta = new HashMap<>();
        empMeta.put("type", "EMPLOYEE");
        Document emp = new Document("Ana García", empMeta);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenAnswer(invocation -> {
                    SearchRequest req = invocation.getArgument(0);
                    String filter = req.getFilterExpression().toString();

                    if (filter.contains("PROJECT")) {
                        return Collections.singletonList(project);
                    } else if (filter.contains("EMPLOYEE")) {
                        return Collections.singletonList(emp);
                    }
                    return Collections.emptyList();
                });

        when(assistantChatUtils.noDataResponse()).thenReturn("Sin coincidencias");

        String response = ragService.handleEmployeesByProject(message, enhancedQuery);

        assertEquals("Sin coincidencias", response);
    }


}

