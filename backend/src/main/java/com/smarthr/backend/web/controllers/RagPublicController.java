package com.smarthr.backend.web.controllers;
import com.smarthr.backend.service.RagSnapshotService;
import com.smarthr.backend.web.dto.CompanyRagSnapshotDto;
import com.smarthr.backend.web.dto.EmployeeCompleteDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class RagPublicController {

    private final RagSnapshotService ragService;

    @Operation(summary = "Snapshot completo de empresa para RAG")
    @ApiResponse(
            responseCode = "200",
            description = "Snapshot RAG de la empresa",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompanyRagSnapshotDto.class)
            )
    )
    @GetMapping("/completeRag")
    public ResponseEntity<CompanyRagSnapshotDto> completeRag() {
        return ResponseEntity.ok(ragService.getCompanyRagSnapshot());
    }
}
