package com.smarthr.assistant.utils;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AssistantChatUtils {

    public String rewriteQuery(String original) {
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

    public String extractEmployeeName(String message) {
        Pattern p = Pattern.compile("([A-ZÁÉÍÓÚÑ][a-záéíóúñ]+\\s+[A-ZÁÉÍÓÚÑ][a-záéíóúñ]+)");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1);
        return null;
    }

    public String noDataResponse() {
        return """
                No dispongo de información interna suficiente para responder a esa consulta.
                Contacte con Recursos Humanos.
                """;
    }

    public String buildContextWithMetadata(List<Document> docs) {
        return docs.stream().map(d -> "📄 " + d.getText()).collect(Collectors.joining("\n---\n"));
    }

    public String extractDepartment(String message) {
        String n = normalize(message);
        if (n.contains("desarrollo")) return "Desarrollo";
        if (n.contains("data")) return "Data";
        if (n.contains("marketing")) return "Marketing";
        if (n.contains("recursos humanos") || n.contains("rrhh")) return "Recursos Humanos";
        return null;
    }
    public String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }

    public String extractProjectLocation(String message) {
        String n = normalize(message);

        if (n.contains("madrid")) return "madrid";
        if (n.contains("barcelona")) return "barcelona";
        if (n.contains("remote") || n.contains("remoto")) return "remote";
        if (n.contains("sevilla")) return "sevilla";

        return null;
    }

    public String extractClientFromMessage(String message) {
        String n = normalize(message);

        // patrones habituales
        if (n.contains("cliente nike") || n.contains("nike")) return "nike";
        if (n.contains("cliente ibm") || n.contains("ibm")) return "ibm";
        if (n.contains("cliente salesforce") || n.contains("salesforce")) return "salesforce";
        if (n.contains("cliente microsoft") || n.contains("microsoft")) return "microsoft";
        if (n.contains("cliente accenture") || n.contains("accenture")) return "accenture";
        if (n.contains("cliente smarthr") || n.contains("smarthr")) return "smarthr";

        return null;
    }

    public String extractProjectCode(String message) {
        String n = normalize(message);

        if (n.contains("PRJ001")) return "PRJ001";
        if (n.contains("PRJ002")) return "PRJ002";
        if (n.contains("PRJ003")) return "PRJ003";
        if (n.contains("PRJ004")) return "PRJ004";
        if (n.contains("PRJ005")) return "PRJ005";
        if (n.contains("PRJ006")) return "PRJ006";

        return null;
    }

    public String extractEmployeeLocation(String message) {
        Pattern p = Pattern.compile("ubicaci[oó]n\\s+en\\s+([A-Za-zÁÉÍÓÚÑáéíóúñ ]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    public boolean containsSkill(String message) {
        String m = normalize(message);
        return m.contains("docker") || m.contains("kubernetes")
                || m.contains("java") || m.contains("spring")
                || m.contains("python");
    }

    public String extractProjectNameFromMessage(String message) {
        String n = normalize(message);

        if (n.contains("optimizacion de procesos")) return "optimizacion de procesos";
        if (n.contains("desarrollo apis")) return "desarrollo apis";
        if (n.contains("portal web")) return "portal web corporativo";
        if (n.contains("migracion cloud")) return "migracion cloud";
        if (n.contains("sistema rrhh")) return "sistema rrhh";

        return null;
    }

    public String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }


}
