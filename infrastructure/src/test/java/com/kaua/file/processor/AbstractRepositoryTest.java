package com.kaua.file.processor;

import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.infrastructure.jdbc.JdbcClientAdapter;
import com.kaua.file.processor.infrastructure.outbox.OutboxJdbcRepository;
import com.kaua.file.processor.infrastructure.outbox.OutboxRepository;
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
    private static final String OUTBOX_TABLE = "outbox";

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private NamedParameterJdbcOperations operations;

    private ImportJobRepository importJobJdbcRepository;
    private OutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        this.outboxRepository = new OutboxJdbcRepository(new JdbcClientAdapter(jdbcClient, operations));
        this.importJobJdbcRepository = new ImportJobJdbcRepository(new JdbcClientAdapter(jdbcClient, operations), outboxRepository);
    }

    protected int countImportJobs() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, IMPORTS_TABLE);
    }

    protected int countOutboxEvents() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, OUTBOX_TABLE);
    }

    public ImportJobRepository importJobRepository() {
        return importJobJdbcRepository;
    }

    public OutboxRepository outboxRepository() {
        return outboxRepository;
    }
}
