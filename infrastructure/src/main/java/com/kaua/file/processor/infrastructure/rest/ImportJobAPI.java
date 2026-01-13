package com.kaua.file.processor.infrastructure.rest;

import com.kaua.file.processor.infrastructure.importjob.res.CreateImportJobResponse;
import com.kaua.file.processor.infrastructure.importjob.res.GetImportJobProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Import Job", description = "Import Job API")
@RequestMapping("/v1/import-jobs")
public interface ImportJobAPI {

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Import a file to be processed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ImportJob created successfully"),
            @ApiResponse(responseCode = "400", description = "A validation error was observed"),
            @ApiResponse(responseCode = "422", description = "A business rule was violated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<CreateImportJobResponse> importFile(@RequestPart MultipartFile file);

    @GetMapping(
            value = "/{importJobId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get the status of an import job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ImportJob retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "ImportJob not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<GetImportJobProgressResponse> getImportJobStatus(@PathVariable("importJobId") String importJobId);
}
