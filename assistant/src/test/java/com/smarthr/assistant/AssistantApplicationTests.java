package com.smarthr.assistant;

import com.smarthr.assistant.component.QueryType;
import com.smarthr.assistant.component.SmartHRQueryRouter;
import com.smarthr.assistant.dto.*;
import com.smarthr.assistant.service.AggregationService;
import com.smarthr.assistant.service.RagService;
import com.smarthr.assistant.service.SmartHRAssistantService;
import com.smarthr.assistant.utils.AssistantChatUtils;
import com.smarthr.assistant.utils.VgVectorInyection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del microservicio IA Assistant
 *
 * NO usa @SpringBootTest para evitar levantar el contexto completo
 * y la conexión a base de datos Docker.
 *
 * Son tests unitarios puros que prueban la lógica de negocio
 * sin dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
class AssistantApplicationTests {

    // ============================================
    // COMPONENTES REALES (sin dependencias)
    // ============================================

    private SmartHRQueryRouter router;
    private AggregationService aggregationService;
    private AssistantChatUtils assistantChatUtils;
    private VgVectorInyection vgVectorInyection;

    // ============================================
    // MOCKS para SmartHRAssistantService
    // ============================================

    @Mock
    private SmartHRQueryRouter mockRouter;

    @Mock
    private RagService mockRagService;

    @Mock
    private AggregationService mockAggregationService;

    private SmartHRAssistantService assistantService;

    @BeforeEach
    void setUp() {
        // Instancias reales (no tienen dependencias externas)
        router = new SmartHRQueryRouter();
        aggregationService = new AggregationService();
        assistantChatUtils = new AssistantChatUtils();
        vgVectorInyection = new VgVectorInyection();

        // Servicio con mocks
        assistantService = new SmartHRAssistantService(
                mockRouter,
                mockRagService,
                mockAggregationService
        );
    }

    @Test
    @DisplayName("Contexto de test carga correctamente")
    void contextLoads() {
        assertNotNull(router);
        assertNotNull(aggregationService);
        assertNotNull(assistantChatUtils);
        assertNotNull(vgVectorInyection);
        assertNotNull(assistantService);
    }

    // ============================================
    // TESTS: SmartHRQueryRouter
    // ============================================

    @Test
    @DisplayName("Router: Debe clasificar saludos como SMALL_TALK")
    void router_whenGreeting_thenClassifyAsSmallTalk() {
        assertEquals(QueryType.SMALL_TALK, router.classify("Hola"));
        assertEquals(QueryType.SMALL_TALK, router.classify("Buenos días"));
        assertEquals(QueryType.SMALL_TALK, router.classify("Hello"));
        assertEquals(QueryType.SMALL_TALK, router.classify("Gracias por tu ayuda"));
    }

    @Test
    @DisplayName("Router: Debe clasificar con mayúsculas/minúsculas como SMALL_TALK")
    void router_whenGreetingWithDifferentCase_thenClassifyAsSmallTalk() {
        assertEquals(QueryType.SMALL_TALK, router.classify("HOLA"));
        assertEquals(QueryType.SMALL_TALK, router.classify("BuEnOs DíAs"));
        assertEquals(QueryType.SMALL_TALK, router.classify("gracias"));
    }

    @Test
    @DisplayName("Router: Debe clasificar consultas de conteo como AGGREGATION")
    void router_whenCountQuery_thenClassifyAsAggregation() {
        assertEquals(QueryType.AGGREGATION, router.classify("¿Cuántos empleados tenemos?"));
        assertEquals(QueryType.AGGREGATION, router.classify("Cuantos proyectos hay"));
        assertEquals(QueryType.AGGREGATION, router.classify("Dame el número de empleados"));
        assertEquals(QueryType.AGGREGATION, router.classify("¿Cuál es el total de ausencias?"));
    }

    @Test
    @DisplayName("Router: Debe clasificar búsquedas específicas como RAG")
    void router_whenSpecificQuery_thenClassifyAsRAG() {
        assertEquals(QueryType.RAG, router.classify("¿Quién trabaja en Madrid?"));
        assertEquals(QueryType.RAG, router.classify("Empleados con experiencia en Docker"));
        assertEquals(QueryType.RAG, router.classify("Proyectos del departamento de Desarrollo"));
    }

    @ParameterizedTest
    @CsvSource({
            "'Hola, ¿cómo estás?', SMALL_TALK",
            "'¿Cuántos empleados?', AGGREGATION",
            "'Buscar empleados en Barcelona', RAG",
            "'Buenos días, necesito ayuda', SMALL_TALK",
            "'Total de proyectos', AGGREGATION"
    })
    @DisplayName("Router: Debe clasificar correctamente varios tipos")
    void router_whenDifferentQueries_thenClassifyCorrectly(String message, QueryType expected) {
        assertEquals(expected, router.classify(message));
    }

    @Test
    @DisplayName("Router: Debe manejar cadenas vacías como RAG")
    void router_whenEmptyString_thenClassifyAsRAG() {
        assertEquals(QueryType.RAG, router.classify(""));
        assertEquals(QueryType.RAG, router.classify("   "));
    }

    // ============================================
    // TESTS: AggregationService
    // ============================================

    @Test
    @DisplayName("Aggregation: Debe responder consulta de cantidad de empleados")
    void aggregation_whenAskingForEmployeeCount_thenReturnMessage() {
        String result = aggregationService.handle("¿Cuántos empleados tenemos?");

        assertNotNull(result);
        assertTrue(result.contains("No dispongo de información agregada"));
        assertTrue(result.contains("panel de RRHH"));
    }

    @Test
    @DisplayName("Aggregation: Debe responder con mayúsculas")
    void aggregation_whenUpperCase_thenReturnMessage() {
        String result = aggregationService.handle("CUÁNTOS EMPLEADOS HAY");
        assertNotNull(result);
        assertTrue(result.contains("No dispongo"));
    }

    @Test
    @DisplayName("Aggregation: Consulta no relacionada con empleados")
    void aggregation_whenNonEmployeeQuery_thenReturnGeneric() {
        String result = aggregationService.handle("¿Cuántos proyectos?");

        assertNotNull(result);
        assertTrue(result.contains("No dispongo de información agregada"));
        assertTrue(result.contains("contacte con RRHH"));
    }

    @Test
    @DisplayName("Aggregation: Debe manejar cadenas vacías")
    void aggregation_whenEmpty_thenReturnGeneric() {
        String result = aggregationService.handle("");
        assertNotNull(result);
        assertTrue(result.contains("No dispongo"));
    }

    // ============================================
    // TESTS: AssistantChatUtils - Normalización
    // ============================================

    @Test
    @DisplayName("Utils: Debe normalizar eliminando acentos")
    void utils_whenTextWithAccents_thenRemoveAccents() {
        assertEquals("maria garcia", assistantChatUtils.normalize("María García"));
        assertEquals("jose", assistantChatUtils.normalize("José"));
        assertEquals("arbol", assistantChatUtils.normalize("árbol"));
    }

    @Test
    @DisplayName("Utils: Debe convertir a minúsculas")
    void utils_whenUpperCase_thenConvertToLowerCase() {
        assertEquals("ana garcia", assistantChatUtils.normalize("ANA GARCIA"));
        assertEquals("hola mundo", assistantChatUtils.normalize("HOLA MUNDO"));
    }

    @Test
    @DisplayName("Utils: Debe eliminar espacios")
    void utils_whenTextWithSpaces_thenTrim() {
        assertEquals("ana garcia", assistantChatUtils.normalize("  Ana García  "));
        assertEquals("hola", assistantChatUtils.normalize(" Hola "));
    }

    @Test
    @DisplayName("Utils: Debe manejar null")
    void utils_whenNull_thenReturnEmpty() {
        assertEquals("", assistantChatUtils.normalize(null));
        assertEquals("", assistantChatUtils.normalize(""));
    }

    // ============================================
    // TESTS: AssistantChatUtils - Extracción
    // ============================================

    @Test
    @DisplayName("Utils: Debe extraer nombre completo")
    void utils_whenValidName_thenExtractName() {
        assertEquals("Ana García", assistantChatUtils.extractEmployeeName("¿Qué proyectos tiene Ana García?"));
        assertEquals("Carlos López", assistantChatUtils.extractEmployeeName("Buscar a Carlos López"));
        assertEquals("María Fernández", assistantChatUtils.extractEmployeeName("María Fernández trabaja en Madrid"));
    }

    @Test
    @DisplayName("Utils: Debe retornar null sin nombre válido")
    void utils_whenNoValidName_thenReturnNull() {
        assertNull(assistantChatUtils.extractEmployeeName("¿Cuántos empleados hay?"));
        assertNull(assistantChatUtils.extractEmployeeName("empleados en Madrid"));
        assertNull(assistantChatUtils.extractEmployeeName("buscar a ana garcia"));
    }

    @ParameterizedTest
    @CsvSource({
            "'Empleados en Madrid', 'madrid'",
            "'¿Quién trabaja en Barcelona?', 'barcelona'",
            "'Proyectos en Sevilla', 'sevilla'",
            "'Ubicación en remote', 'remote'"
    })
    @DisplayName("Utils: Debe extraer ubicaciones")
    void utils_whenLocation_thenExtract(String message, String expected) {
        String location = assistantChatUtils.extractProjectLocation(message);
        assertEquals(expected, location);
    }

    @Test
    @DisplayName("Utils: Debe extraer departamentos")
    void utils_whenDepartment_thenExtract() {
        assertEquals("Desarrollo", assistantChatUtils.extractDepartment("departamento de desarrollo"));
        assertEquals("Data", assistantChatUtils.extractDepartment("Equipo de data"));
        assertEquals("Marketing", assistantChatUtils.extractDepartment("departamento marketing"));
        assertEquals("Recursos Humanos", assistantChatUtils.extractDepartment("RRHH"));
    }

    @Test
    @DisplayName("Utils: Debe retornar null sin departamento")
    void utils_whenNoDepartment_thenReturnNull() {
        assertNull(assistantChatUtils.extractDepartment("Empleados en Madrid"));
    }

    @ParameterizedTest
    @CsvSource({
            "'Proyectos del cliente Nike', 'nike'",
            "'Cliente IBM', 'ibm'",
            "'proyecto para Salesforce', 'salesforce'",
            "'CLIENTE MICROSOFT', 'microsoft'"
    })
    @DisplayName("Utils: Debe extraer clientes")
    void utils_whenClient_thenExtract(String message, String expected) {
        assertEquals(expected, assistantChatUtils.extractClientFromMessage(message));
    }

    @Test
    @DisplayName("Utils: Debe extraer códigos de proyecto")
    void utils_whenProjectCode_thenExtract() {
        assertEquals("PRJ001", assistantChatUtils.extractProjectCode("Proyecto PRJ001"));
        assertEquals("PRJ002", assistantChatUtils.extractProjectCode("Código PRJ002"));
        assertEquals("PRJ006", assistantChatUtils.extractProjectCode("prj006"));
    }

    @Test
    @DisplayName("Utils: Debe retornar null sin código")
    void utils_whenNoProjectCode_thenReturnNull() {
        assertNull(assistantChatUtils.extractProjectCode("Proyectos activos"));
    }

    @Test
    @DisplayName("Utils: Debe extraer nombres de proyectos")
    void utils_whenKnownProject_thenExtractName() {
        assertEquals("optimizacion de procesos",
                assistantChatUtils.extractProjectNameFromMessage("Empleados en optimización de procesos"));
        assertEquals("portal web corporativo",
                assistantChatUtils.extractProjectNameFromMessage("¿Quién trabaja en el portal web?"));
    }

    @Test
    @DisplayName("Utils: Debe detectar skills")
    void utils_whenSkillPresent_thenReturnTrue() {
        assertTrue(assistantChatUtils.containsSkill("Empleados con Docker"));
        assertTrue(assistantChatUtils.containsSkill("experiencia en Kubernetes"));
        assertTrue(assistantChatUtils.containsSkill("desarrollador Java"));
        assertFalse(assistantChatUtils.containsSkill("Empleados en Madrid"));
    }

    @Test
    @DisplayName("Utils: Debe capitalizar")
    void utils_whenLowerCase_thenCapitalize() {
        assertEquals("Madrid", assistantChatUtils.capitalize("madrid"));
        assertEquals("Ana", assistantChatUtils.capitalize("ana"));
        assertNull(assistantChatUtils.capitalize(null));
        assertEquals("", assistantChatUtils.capitalize(""));
    }

    @Test
    @DisplayName("Utils: Debe enriquecer consultas")
    void utils_whenQuery_thenEnrich() {
        String result = assistantChatUtils.rewriteQuery("Ausencias de Ana García");
        assertTrue(result.contains("Ausencias"));
        assertTrue(result.contains("solicitud"));

        result = assistantChatUtils.rewriteQuery("Buscar empleados");
        assertTrue(result.contains("empleado"));
        assertTrue(result.contains("departamento"));
    }

    @Test
    @DisplayName("Utils: Debe retornar mensaje sin datos")
    void utils_whenNoData_thenReturnMessage() {
        String result = assistantChatUtils.noDataResponse();
        assertNotNull(result);
        assertTrue(result.contains("No dispongo"));
        assertTrue(result.contains("Recursos Humanos"));
    }

    // ============================================
    // TESTS: VgVectorInyection
    // ============================================

    @Test
    @DisplayName("Vector: Debe convertir empleado completo a documento")
    void vector_whenCompleteEmployee_thenCreateDocument() {
        EmployeeCompleteDto employee = createCompleteEmployee();

        Document doc = vgVectorInyection.employeeToDoc(employee);

        assertNotNull(doc);
        assertNotNull(doc.getId());
        assertTrue(doc.getId().startsWith("employee:"));
        assertTrue(doc.getId().contains("ana-garcía"));

        String content = doc.getText();
        assertTrue(content.contains("Ana García"));
        assertTrue(content.contains("Senior Backend Developer"));
        assertTrue(content.contains("Desarrollo"));
        assertTrue(content.contains("Madrid"));
        assertTrue(content.contains("55000"));

        assertEquals("smarthr", doc.getMetadata().get("source"));
        assertEquals("EMPLOYEE", doc.getMetadata().get("type"));
        assertEquals("Madrid", doc.getMetadata().get("location"));
    }

    @Test
    @DisplayName("Vector: Debe manejar empleado sin proyectos")
    void vector_whenEmployeeWithoutProjects_thenCreateDocument() {
        EmployeeCompleteDto employee = EmployeeCompleteDto.builder()
                .name("Carlos López")
                .email("carlos.lopez@smarthr.com")
                .jobPosition("Junior Developer")
                .department("Desarrollo")
                .location("Barcelona")
                .hireDate(LocalDate.parse("2024-01-15"))
                .skills(Arrays.asList("Java", "Spring Boot"))
                .contractType("TEMPORAL")
                .weeklyHours(40)
                .contractStartDate(LocalDate.parse("2024-01-15"))
                .baseSalary(new BigDecimal("35000.00"))
                .projectsInfo(Collections.emptyList())
                .build();

        Document doc = vgVectorInyection.employeeToDoc(employee);

        assertNotNull(doc);
        assertTrue(doc.getText().contains("sin proyectos asignados"));
    }


    @Test
    @DisplayName("Vector: Debe convertir proyecto a documento")
    void vector_whenProject_thenCreateDocument() {
        ProjectRagDto project = new ProjectRagDto(
                "PRJ001",
                "Portal Web Corporativo",
                "Nike",
                "Madrid",
                LocalDate.parse("2024-01-15"),
                null
        );

        Document doc = vgVectorInyection.projectToDoc(project);

        assertNotNull(doc);
        assertTrue(doc.getText().contains("Portal Web Corporativo"));
        assertTrue(doc.getText().contains("PRJ001"));
        assertTrue(doc.getText().contains("Nike"));
        assertTrue(doc.getText().contains("Actualmente activo"));

        assertEquals("PROJECT", doc.getMetadata().get("type"));
        assertEquals("Portal Web Corporativo", doc.getMetadata().get("projectName"));
    }

    @Test
    @DisplayName("Vector: Debe manejar proyecto finalizado")
    void vector_whenCompletedProject_thenIncludeEndDate() {
        ProjectRagDto project = new ProjectRagDto(
                "PRJ002",
                "Migración Cloud",
                "IBM",
                "Remote",
                LocalDate.parse("2023-01-01"),
                LocalDate.parse("2024-12-31")
        );

        Document doc = vgVectorInyection.projectToDoc(project);

        assertTrue(doc.getText().contains("Fecha de finalización: 2024-12-31"));
        assertFalse(doc.getText().contains("Actualmente activo"));
    }

    @Test
    @DisplayName("Vector: Debe convertir skill a documento")
    void vector_whenSkill_thenCreateDocument() {
        SkillRagDto skill = new SkillRagDto("Docker", "Containerization platform");

        Document doc = vgVectorInyection.skillToDoc(skill);

        assertNotNull(doc);
        assertTrue(doc.getText().contains("Docker"));
        assertTrue(doc.getText().contains("Containerization platform"));
        assertEquals("SKILL", doc.getMetadata().get("type"));
    }

    @Test
    @DisplayName("Vector: Debe convertir departamento a documento")
    void vector_whenDepartment_thenCreateDocument() {
        DepartmentRagDto dept = new DepartmentRagDto(
                "Desarrollo",
                "Equipo de desarrollo de software"
        );

        Document doc = vgVectorInyection.departmentToDoc(dept);

        assertNotNull(doc);
        assertTrue(doc.getText().contains("Desarrollo"));
        assertTrue(doc.getText().contains("Equipo de desarrollo"));
        assertEquals("DEPARTMENT", doc.getMetadata().get("type"));
    }

    @Test
    @DisplayName("Vector: Debe convertir ausencia a documento")
    void vector_whenLeaveRequest_thenCreateDocument() {
        PendingLeaveRequestRagDto leave = new PendingLeaveRequestRagDto(
                "Ana García",
                "PENDING",
                "VACATION",
                LocalDate.parse("2026-03-01"),
                LocalDate.parse("2026-03-15"),
                "Vacaciones familiares"
        );

        Document doc = vgVectorInyection.leaveRequestToDoc(leave);

        assertNotNull(doc);
        assertTrue(doc.getId().contains("ana-garcía"));
        assertTrue(doc.getText().contains("Ana García"));
        assertTrue(doc.getText().contains("VACATION"));
        assertTrue(doc.getText().contains("PENDING"));

        assertEquals("LEAVE_REQUEST", doc.getMetadata().get("type"));
        assertEquals("PENDING", doc.getMetadata().get("status"));
    }

    @Test
    @DisplayName("Vector: Debe manejar ausencia sin comentarios")
    void vector_whenLeaveRequestWithoutComments_thenUseDefault() {
        PendingLeaveRequestRagDto leave = new PendingLeaveRequestRagDto(
                "Carlos López",
                "APPROVED",
                "SICK_LEAVE",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-03"),
                null
        );

        Document doc = vgVectorInyection.leaveRequestToDoc(leave);
        assertTrue(doc.getText().contains("No especificado"));
    }

    // ============================================
    // TESTS: SmartHRAssistantService
    // ============================================

    @Test
    @DisplayName("Assistant: Debe responder SMALL_TALK con saludo")
    void assistant_whenSmallTalk_thenReturnGreeting() {
        String message = "Hola";
        when(mockRouter.classify(message)).thenReturn(QueryType.SMALL_TALK);

        String response = assistantService.chat(message, null);

        assertNotNull(response);
        assertTrue(response.contains("Hola"));
        assertTrue(response.contains("SmartHR"));
        verify(mockRouter).classify(message);
        verifyNoInteractions(mockRagService, mockAggregationService);
    }

    @Test
    @DisplayName("Assistant: Debe delegar AGGREGATION")
    void assistant_whenAggregation_thenDelegate() {
        String message = "¿Cuántos empleados?";
        when(mockRouter.classify(message)).thenReturn(QueryType.AGGREGATION);
        when(mockAggregationService.handle(message)).thenReturn("Respuesta agregación");

        String response = assistantService.chat(message, null);

        assertEquals("Respuesta agregación", response);
        verify(mockRouter).classify(message);
        verify(mockAggregationService).handle(message);
        verifyNoInteractions(mockRagService);
    }

    @Test
    @DisplayName("Assistant: Debe delegar RAG sin employeeId")
    void assistant_whenRAGWithoutEmployee_thenDelegateToGeneral() {
        String message = "Empleados en Madrid";
        when(mockRouter.classify(message)).thenReturn(QueryType.RAG);
        when(mockRagService.chatWithRag(message)).thenReturn("Respuesta RAG");

        String response = assistantService.chat(message, null);

        assertEquals("Respuesta RAG", response);
        verify(mockRouter).classify(message);
        verify(mockRagService).chatWithRag(message);
        verify(mockRagService, never()).chatForEmployee(anyString(), anyString());
    }

    @Test
    @DisplayName("Assistant: Debe delegar RAG con employeeId")
    void assistant_whenRAGWithEmployee_thenDelegateToEmployee() {
        String message = "¿Cuánto gano?";
        String employeeId = "ana.garcia";
        when(mockRouter.classify(message)).thenReturn(QueryType.RAG);
        when(mockRagService.chatForEmployee(employeeId, message))
                .thenReturn("Tu salario es...");

        String response = assistantService.chat(message, employeeId);

        assertEquals("Tu salario es...", response);
        verify(mockRouter).classify(message);
        verify(mockRagService).chatForEmployee(employeeId, message);
        verify(mockRagService, never()).chatWithRag(anyString());
    }

    @Test
    @DisplayName("Assistant: Debe manejar mensajes vacíos")
    void assistant_whenEmpty_thenHandle() {
        when(mockRouter.classify("")).thenReturn(QueryType.RAG);
        when(mockRagService.chatWithRag("")).thenReturn("Respuesta vacía");

        String response = assistantService.chat("", null);

        assertNotNull(response);
        verify(mockRouter).classify("");
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private EmployeeCompleteDto createCompleteEmployee() {
        ProjectRagDto project = new ProjectRagDto(
                "PRJ001",
                "Portal Web",
                "Nike",
                "Madrid",
                LocalDate.parse("2024-01-15"),
                null
        );

        return EmployeeCompleteDto.builder()
                .name("Ana García")
                .email("ana.garcia@smarthr.com")
                .jobPosition("Senior Backend Developer")
                .department("Desarrollo")
                .location("Madrid")
                .hireDate(LocalDate.parse("2022-03-15"))
                .skills(Arrays.asList("Java", "Spring Boot", "Docker"))
                .contractType("PERMANENTE")
                .weeklyHours(40)
                .contractStartDate(LocalDate.parse("2022-03-15"))
                .baseSalary(new BigDecimal("55000.00"))
                .bonus(new BigDecimal("5000.00"))
                .projectsInfo(Collections.singletonList(project))
                .build();
    }

}