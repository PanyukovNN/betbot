package com.zylex.betbot.controller.logger;

import com.zylex.betbot.exception.ConsoleLoggerException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ConsoleLogger {

    private volatile static String logOutput;

    static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    static {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm a");
        LocalDateTime startDateTime = LocalDateTime.now();
        logOutput = "\n\n" + StringUtils.repeat("-", 100) + "\n"
                + String.format("Bot started at: %s", startDateTime.format(formatter))
                + "\n" + StringUtils.repeat("-", 50) + "\n";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeToLogFile() {
        try {
            File logFile = new File("results/log.txt");
            new File("results").mkdir();
            logFile.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logOutput);
            }
        } catch (IOException e) {
            throw new ConsoleLoggerException(e.getMessage(), e);
        }
    }

    private synchronized static void addToLog(String line) {
        if (line.contains("\b")) {
            int a = StringUtils.countMatches(line, "\b");
            int b = logOutput.lastIndexOf("\n");
            logOutput = logOutput.substring(0, Math.max(logOutput.length() - a, b + 1))
                    + line.replace("\b", "");
        } else {
            logOutput += line;
        }
    }

    public synchronized static void writeExceptionToLog(String message) {
        logOutput += "\n" + message;
    }

    void writeLineSeparator() {
        String line = "\n" + StringUtils.repeat("-", 50);
        writeInLine(line);
    }

    void writeErrorMessage(String line) {
        System.err.print(line);
        addToLog(line);
    }

    synchronized void writeInLine(String line) {
        System.out.print(line);
        addToLog(line);
    }

    String computeTime(long startTime) {
        long millis = System.currentTimeMillis() - startTime;
        String time = String.format("%02d min. %02d sec.",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours > 0) {
            return String.format("%02d h. ", hours) + time;
        } else {
            return time;
        }
    }
}
