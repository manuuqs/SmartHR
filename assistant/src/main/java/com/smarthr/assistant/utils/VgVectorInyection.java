package com.smarthr.assistant.utils;

import com.smarthr.assistant.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class VgVectorInyection {

    public void upsertDocuments(List<Document> documents, VectorStore vectorStore) {
        for (Document doc : documents) {
            try {
                vectorStore.delete(List.of(doc.getId()));
            } catch (Exception ignored) {
            }
            vectorStore.add(List.of(doc));
        }
    }


    public Document employeeToDoc(EmployeeCompleteDto emp) {

        Map<String,Object> metadata = new HashMap<>();
        metadata.put("source", "smarthr");
        metadata.put("type", "EMPLOYEE");
        metadata.put("entityId", "employee:" + emp.id());
        metadata.put("jobPosition", emp.jobPosition());
        metadata.put("location", emp.location());
        metadata.put("department", emp.department());
        metadata.put("updatedAt", Instant.now().toString());

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

    public Document projectToDoc(ProjectRagDto p) {

        Map<String,Object> metadata = new HashMap<>();
        metadata.put("source", "smarthr");
        metadata.put("type", "PROJECT");
        metadata.put("entityId", "project:" + p.code());
        metadata.put("projectName", p.name());   // 🔥 CLAVE
        metadata.put("client", p.client());
        metadata.put("ubication", p.ubication());

        String content = """
            Proyecto interno de la empresa SmartHR llamado %s (código %s).
            Cliente: %s. Ubicación principal: %s.
            Inicio del proyecto: %s. %s.
            """
                .formatted(
                        p.name(),
                        p.code(),
                        p.client(),
                        p.ubication(),
                        p.startDate(),
                        p.endDate() != null ? "Fecha de finalización: " + p.endDate() : "Actualmente activo"
                );

        return new Document(content, metadata);
    }

    public Document skillToDoc(SkillRagDto s) {
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

    public Document departmentToDoc(DepartmentRagDto d) {
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

    public Document leaveRequestToDoc(PendingLeaveRequestRagDto l ) {

        String id = "leave:" + l.employeeName().toLowerCase().replace(" ", "-")
                + ":" + l.startDate();

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
                Optional.ofNullable(l.comments()).orElse("No especificado")
        );

        return new Document(
                id,
                content,
                Map.of(
                        "type", "LEAVE_REQUEST",
                        "source", "smarthr",
                        "status", l.status(),
                        "entityId", id,
                        "leaveType", l.type()
                )
        );
    }

    public void upsertLeaveRequest(LeaveRequestRagDto dto, VectorStore vectorStore) {

        String entityId = "leave:" + Normalizer.normalize(dto.employeeName(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace(" ", "-")
                .toLowerCase()
                + ":" + dto.startDate();

        String content = """
            Solicitud de ausencia.
            Empleado: %s
            Estado de la solicitud: %s
            Tipo: %s
            Periodo: %s → %s
            Comentarios: %s
            """.formatted(
                dto.employeeName(),
                dto.status(),
                dto.type(),
                dto.startDate(),
                dto.endDate(),
                dto.comments() != null ? dto.comments() : "-"
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "LEAVE_REQUEST");
        metadata.put("source", "smarthr");
        metadata.put("status", dto.status());
        metadata.put("leaveType", dto.type());
        metadata.put("entityId", entityId);

        Document document = new Document(entityId, content, metadata);

        try {
            vectorStore.delete(List.of(entityId)); // <-- usar entityId directamente
        } catch (Exception ignored) {
        }

        vectorStore.add(List.of(document));

    }

    public void insertLeaveRequest(LeaveRequestRagDto dto, VectorStore vectorStore) {

        String entityId = "leave:" + Normalizer.normalize(dto.employeeName(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace(" ", "-")
                .toLowerCase()
                + ":" + dto.startDate();

        String content = """
            Solicitud de ausencia.
            Empleado: %s
            Estado de la solicitud: %s
            Tipo: %s
            Periodo: %s → %s
            Comentarios: %s
            """.formatted(
                dto.employeeName(),
                dto.status(),
                dto.type(),
                dto.startDate(),
                dto.endDate(),
                dto.comments() != null ? dto.comments() : "-"
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "LEAVE_REQUEST");
        metadata.put("source", "smarthr");
        metadata.put("status", dto.status());
        metadata.put("leaveType", dto.type());
        metadata.put("entityId", entityId);

        Document document = new Document(entityId, content, metadata);

        vectorStore.add(List.of(document));

    }



}
