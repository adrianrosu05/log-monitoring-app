import models.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.LogProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogProcessorIntegrationTest {

    private Path tempLogFile;
    private Path tempReportFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary log file with test data
        tempLogFile = Files.createTempFile("test_logs", ".log");
        tempReportFile = Files.createTempFile("test_report", ".log");

        List<String> logContent = List.of(
                "12:00:00,Job1,START,1001",
                "12:10:00,Job1,END,1001",
                "12:05:00,Job2,START,1002",
                "12:07:00,Job2,END,1002"
        );

        Files.write(tempLogFile, logContent);
    }

    @Test
    void testFullWorkflow() throws IOException {
        // Step 1: Parse logs
        List<Log> logs = LogProcessor.parseLogs(tempLogFile.toString());
        assertEquals(4, logs.size()); // 4 log entries

        // Step 2: Process logs
        List<String> report = LogProcessor.processLogs(logs);
        assertEquals(2, report.size()); // 2 jobs processed

        // Step 3: Write the report to a file
        LogProcessor.writeReport(tempReportFile.toString(), report);

        // Step 4: Verify the report content
        List<String> generatedReport = Files.readAllLines(tempReportFile);
        assertEquals(2, generatedReport.size()); // Expecting two job reports
        assertTrue(generatedReport.get(0).contains("Job1"));
        assertTrue(generatedReport.get(1).contains("Job2"));
        assertTrue(generatedReport.get(0).contains("10 min 0 sec")); // Job1 duration
        assertTrue(generatedReport.get(1).contains("2 min 0 sec"));  // Job2 duration
    }

    @AfterEach
    void tearDown() throws IOException {
        // Cleanup temporary files
        Files.deleteIfExists(tempLogFile);
        Files.deleteIfExists(tempReportFile);
    }
}
