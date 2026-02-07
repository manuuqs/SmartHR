package com.smarthr.assistant.utils;

import com.smarthr.assistant.dto.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VgVectorInyection {

    public void upsertDocuments(List<Document> documents, VectorStore vectorStore) {
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

    public Document leaveRequestToDoc(PendingLeaveRequestRagDto l) {
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


}
