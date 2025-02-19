package org.lseg;

import models.Log;
import services.LogProcessor;
import utils.LogConstants;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<Log> logEntries = LogProcessor.parseLogs(LogConstants.LOG_FILE);
        List<String> report = LogProcessor.processLogs(logEntries);
        LogProcessor.writeReport(LogConstants.REPORT_FILE, report);
    }
}