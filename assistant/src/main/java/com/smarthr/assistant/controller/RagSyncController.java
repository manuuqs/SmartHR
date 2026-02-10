package com.smarthr.assistant.controller;

import com.smarthr.assistant.dto.EmployeeCompleteDto;
import com.smarthr.assistant.dto.LeaveRequestRagDto;
import com.smarthr.assistant.service.RagService;
import com.smarthr.assistant.utils.VgVectorInyection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/rag")
@RequiredArgsConstructor
@Slf4j
public class RagSyncController {

    private final RagService ragService;

    @Autowired
    private VgVectorInyection vgVectorInyection;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private PgVectorStore pgvectorStore;


    @PostMapping("/upsert-employee")
    public ResponseEntity<String> upsertEmployee(@RequestBody EmployeeCompleteDto employeeDto) {
        log.info("upsertEmployee {}", employeeDto);
        try {
            // Convertir el DTO a documentos y guardarlos
            List<Document> documents = ragService.buildEmployee(employeeDto);
            vgVectorInyection.upsertDocuments(documents, vectorStore);

            return ResponseEntity.ok("Empleado insertado en RAG correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error insertando empleado en RAG: " + e.getMessage());
        }
    }

    @PostMapping("/upsert-leave-request")
    public ResponseEntity<Void> upsertLeaveRequest(
            @RequestBody LeaveRequestRagDto dto
    ) {
        log.info("upsertLeaveRequest {}", dto);
        vgVectorInyection.upsertLeaveRequest(dto, pgvectorStore);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/insert-leave-request")
    public ResponseEntity<Void> insertLeaveRequest(
            @RequestBody LeaveRequestRagDto dto
    ) {
        log.info("insertLeaveRequest {}", dto);
        vgVectorInyection.insertLeaveRequest(dto, pgvectorStore);
        return ResponseEntity.ok().build();
    }


}
