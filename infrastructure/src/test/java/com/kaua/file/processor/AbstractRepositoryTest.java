package com.kaua.file.processor;

import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.infrastructure.jdbc.JdbcClientAdapter;
import com.kaua.file.processor.infrastructure.repositories.ImportJobJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

@DataJdbcTest
@Tag("integrationTest")
@ActiveProfiles("test-integration")
public abstract class AbstractRepositoryTest {

    private static final String IMPORTS_TABLE = "import_jobs";

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private NamedParameterJdbcOperations operations;

    private ImportJobJdbcRepository importJobJdbcRepository;

    @BeforeEach
    void setUp() {
        this.importJobJdbcRepository = new ImportJobJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
    }

    protected int countImportJobs() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, IMPORTS_TABLE);
    }

    public ImportJobRepository importJobRepository() {
        return importJobJdbcRepository;
    }
}
