package com.kaua.file.processor.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaua.file.processor.ApiTest;
import com.kaua.file.processor.ControllerTest;
import com.kaua.file.processor.application.importjob.create.CreateImportJobCommand;
import com.kaua.file.processor.application.importjob.create.CreateImportJobOutput;
import com.kaua.file.processor.application.importjob.create.CreateImportJobUseCase;
import com.kaua.file.processor.domain.utils.ULID;
import com.kaua.file.processor.infrastructure.idempotency.IdempotencyKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(controllers = ImportJobAPI.class)
class ImportJobAPITest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreateImportJobUseCase createImportJobUseCase;

    @Captor
    private ArgumentCaptor<CreateImportJobCommand> createImportJobCommandCaptor;

    @Test
    void givenAValidFile_whenCallsImportFile_shouldReturnImportJobCreated() throws Exception {
        final var aFile = createMultipartFile();

        final var expectedImportJobId = "123e4567-e89b-12d3-a456-426614174000";
        final var expectedStatus = "CREATED";

        Mockito.when(createImportJobUseCase.execute(any()))
                .thenReturn(new CreateImportJobOutput(
                        expectedImportJobId,
                        aFile.getOriginalFilename(),
                        expectedStatus
                ));

        final var aRequest = MockMvcRequestBuilders.multipart("/v1/import-jobs")
                .file(aFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(ApiTest.admin())
                .header(IdempotencyKey.IDEMPOTENCY_KEY_HEADER, ULID.random().toString())
                .accept(MediaType.APPLICATION_JSON);

        final var aResponse = this.mvc.perform(aRequest);

        aResponse
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.import_job_id").value(expectedImportJobId))
                .andExpect(jsonPath("$.file_name").value(aFile.getOriginalFilename()))
                .andExpect(jsonPath("$.status").value(expectedStatus));

        Mockito.verify(createImportJobUseCase, Mockito.times(1)).execute(createImportJobCommandCaptor.capture());

        final var aCommand = createImportJobCommandCaptor.getValue();

        Assertions.assertEquals(aFile.getOriginalFilename(), aCommand.fileName());
    }

    @Test
    void givenAnInvalidFile_whenIOExceptionOccurs_shouldReturnInternalServerError() throws Exception {
        final var aFile = Mockito.mock(MockMultipartFile.class);

        Mockito.when(aFile.getOriginalFilename())
                .thenReturn("file.csv");

        Mockito.when(aFile.getInputStream())
                .thenThrow(new IOException("boom"));

        final var aRequest = MockMvcRequestBuilders.multipart("/v1/import-jobs")
                .file(aFile)
                .header(IdempotencyKey.IDEMPOTENCY_KEY_HEADER, ULID.random().toString())
                .with(ApiTest.admin())
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(aRequest)
                .andExpect(status().isInternalServerError());

        Mockito.verifyNoInteractions(createImportJobUseCase);
    }

    private MockMultipartFile createMultipartFile() {
        return new MockMultipartFile(
                "file",
                "file.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "id,name\n1,john".getBytes()
        );
    }
}
