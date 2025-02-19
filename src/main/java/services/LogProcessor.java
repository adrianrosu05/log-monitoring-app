package services;

import models.Log;
import models.LogStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.LogConstants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogProcessor {

    private static final Logger log = LoggerFactory.getLogger(LogProcessor.class);
    private static final Duration WARNING_THRESHOLD = Duration.ofMinutes(5);
    private static final Duration ERROR_THRESHOLD = Duration.ofMinutes(10);

    public static List<Log> parseLogs(String filePath) {
        List<Log> logEntries = new ArrayList<>();

        Path path = Paths.get(filePath);
        //Check if the file exists before trying to read it
        if (!Files.exists(path)) {
            log.error("Log file not found: {}", filePath);
            return logEntries;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue; // Skip invalid lines

                LocalTime timestamp = LocalTime.parse(parts[0]);
                String jobName = parts[1].trim();
                String statusString = parts[2].trim();
                int pid = Integer.parseInt(parts[3].trim());

                try {
                    LogStatus status = LogStatus.valueOf(statusString.toUpperCase()); // Convert String to Enum
                    logEntries.add(new Log(timestamp, jobName, status, pid));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid log status: {}", statusString);
                }
            }
        } catch (IOException e) {
            log.error("Error reading the log file: {}", e.getMessage());
        }

        if (logEntries.isEmpty()) {
            log.warn("No valid log entries found in file: {}", filePath);
        }

        return logEntries;
    }

    public static List<String> processLogs(List<Log> logEntries) {
        Map<Integer, Log> jobStartTimes = new HashMap<>();
        List<String> reportLines = new ArrayList<>();

        for (Log entry : logEntries) {
            if (LogStatus.START.equals(entry.getStatus())) {
                jobStartTimes.put(entry.getPid(), entry);
            } else if (LogStatus.END.equals(entry.getStatus())) {
                Log startEntry = jobStartTimes.remove(entry.getPid());

                if (startEntry != null) {
                    Duration duration = Duration.between(startEntry.getTimestamp(), entry.getTimestamp());
                    String status = getStatus(duration);

                    String report = String.format("Job %s (PID %d) Start: %s, End: %s, Duration: %s - %s",
                            startEntry.getJobDescription(), entry.getPid(),
                            startEntry.getTimestamp(), entry.getTimestamp(),
                            formatDuration(duration), status);

                    reportLines.add(report);
                }
            }
        }

        return reportLines;
    }

    static String getStatus(Duration duration) {
        if (duration.compareTo(ERROR_THRESHOLD) > 0) {
            return LogConstants.ERROR;
        } else if (duration.compareTo(WARNING_THRESHOLD) > 0) {
            return LogConstants.WARNING;
        } else {
            return LogConstants.OK;
        }
    }

    private static String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%d min %d sec", minutes, seconds);
    }

    public static void writeReport(String filePath, List<String> reportLines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : reportLines) {
                log.info(line);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Error writing the file", e);
        }
    }
}

