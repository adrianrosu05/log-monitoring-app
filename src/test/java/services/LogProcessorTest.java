package services;

import models.Log;
import models.LogStatus;
import org.junit.jupiter.api.Test;
import utils.LogConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LogProcessorTest {

    @Test
    void testParseLogsValidFile() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("logs", ".txt");
        Files.write(tempFile, List.of(
                "12:30:45,Job1,START,123",
                "12:35:45,Job1,END,123"
        ));

        // Act
        List<Log> logs = LogProcessor.parseLogs(tempFile.toString());

        // Assert
        assertEquals(2, logs.size());
        assertEquals(LocalTime.of(12, 30, 45), logs.get(0).getTimestamp());
        assertEquals("Job1", logs.get(0).getJobDescription());
        assertEquals(LogStatus.START, logs.get(0).getStatus());
        assertEquals(123, logs.get(0).getPid());

        Files.delete(tempFile);
    }

    @Test
    void testParseLogsInvalidLines() throws IOException {
        Path tempFile = Files.createTempFile("logs", ".txt");
        Files.write(tempFile, List.of(
                "Invalid line",
                "14:00:00,Job3,UNKNOWN,789",
                "15:30:20,Job4,START,999"
        ));

        List<Log> logs = LogProcessor.parseLogs(tempFile.toString());

        assertEquals(1, logs.size());
        assertEquals("Job4", logs.get(0).getJobDescription());

        Files.delete(tempFile);
    }

    @Test
    void testParseLogsEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile("logs", ".txt");
        Files.write(tempFile, List.of());
        List<Log> logs = LogProcessor.parseLogs(tempFile.toString());
        assertTrue(logs.isEmpty());
        Files.delete(tempFile);
    }

    @Test
    void testProcessLogsValidLogs() {
        // Arrange
        List<Log> logs = List.of(
                new Log(LocalTime.of(12, 0), "Job1", LogStatus.START, 1001),
                new Log(LocalTime.of(12, 10), "Job1", LogStatus.END, 1001)
        );

        // Act
        List<String> reports = LogProcessor.processLogs(logs);

        // Assert
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains("Job1"));
        assertTrue(reports.get(0).contains("10 min 0 sec"));
    }

    @Test
    void testProcessLogsJobWithoutStart() {
        // Arrange
        List<Log> logs = List.of(
                new Log(LocalTime.of(12, 10), "Job1", LogStatus.END, 1001)
        );

        // Act
        List<String> reports = LogProcessor.processLogs(logs);

        // Assert
        assertTrue(reports.isEmpty());
    }

    @Test
    void testProcessLogsJobWithoutEnd() {
        // Arrange
        List<Log> logs = List.of(
                new Log(LocalTime.of(12, 0), "Job1", LogStatus.START, 1001)
        );

        // Act
        List<String> reports = LogProcessor.processLogs(logs);

        // Assert// without END, no report
    }

    @Test
    void testProcessLogsEmptyLogList() {
        // Act
        List<String> reports = LogProcessor.processLogs(List.of());

        // Assert
        assertTrue(reports.isEmpty());
    }

    @Test
    void testProcessLogsLongJobDurationWarning() {
        // Arrange
        List<Log> logs = List.of(
                new Log(LocalTime.of(12, 0), "Job1", LogStatus.START, 1001),
                new Log(LocalTime.of(12, 7), "Job1", LogStatus.END, 1001) // 7 minute (WARNING threshold)
        );

        // Act
        List<String> reports = LogProcessor.processLogs(logs);

        // Assert
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains(LogConstants.WARNING));
    }

    @Test
    void testProcessLogsLongJobDurationError() {
        // Arrange
        List<Log> logs = List.of(
                new Log(LocalTime.of(12, 0), "Job1", LogStatus.START, 1001),
                new Log(LocalTime.of(12, 15), "Job1", LogStatus.END, 1001) // 15 minute (ERROR threshold)
        );

        // Act
        List<String> reports = LogProcessor.processLogs(logs);

        // Assert
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains(LogConstants.ERROR));
    }

    @Test
    void testGetStatusOK() {
        Duration shortDuration = Duration.ofMinutes(4).plusSeconds(59); // 4 min 59 sec (under threshold)

        // Act
        String status = LogProcessor.getStatus(shortDuration);

        // Assert
        assertEquals(LogConstants.OK, status);
    }

    @Test
    void testWriteReportValidFile() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("report", ".txt");
        List<String> reportLines = List.of(
                "Job1 (PID 1001) Start: 12:00, End: 12:10, Duration: 10 min 0 sec - OK",
                "Job2 (PID 1002) Start: 12:05, End: 12:07, Duration: 2 min 0 sec - OK"
        );

        // Act
        LogProcessor.writeReport(tempFile.toString(), reportLines);

        // Assert
        List<String> writtenLines = Files.readAllLines(tempFile);
        assertEquals(reportLines, writtenLines);

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    void testWriteReportEmptyFile() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("emptyReport", ".txt");
        List<String> reportLines = List.of();

        // Act
        LogProcessor.writeReport(tempFile.toString(), reportLines);

        // Assert
        List<String> writtenLines = Files.readAllLines(tempFile);
        assertTrue(writtenLines.isEmpty());

        // Cleanup
        Files.delete(tempFile);
    }

    @Test
    void testWriteReportErrorWriting() {
        // Arrange
        String invalidPath = "/invalid_directory/report.txt";
        List<String> reportLines = List.of("Test report");

        // Act & Assert
        assertDoesNotThrow(() -> LogProcessor.writeReport(invalidPath, reportLines));
    }
}


