package com.zylex.betbot.controller.logger;

import com.zylex.betbot.BetBotApplication;
import com.zylex.betbot.exception.ConsoleLoggerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for loggers.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ConsoleLogger {

    private final static Logger LOG = LoggerFactory.getLogger(BetBotApplication.class);

    static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    /**
     * Writes start message
     */
    public static void startMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a dd.MM.yyyy");
        LocalDateTime startDateTime = LocalDateTime.now();
        String startMessage = String.format("BetBot started at: %s", startDateTime.format(formatter));
        writeInLine(startMessage);
        LOG.info(startMessage);
        writeLineSeparator();
    }

    static void writeLineSeparator() {
        String line = "\n" + StringUtils.repeat("-", 50);
        writeInLine(line);
    }

    public synchronized static void writeErrorMessage(String message, Throwable cause) {
        System.err.print(message);
        LOG.error(message, cause);
    }

    public synchronized static void writeInLine(String line) {
        System.out.print(line);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    synchronized void pressAnyButton() {
        try {
            System.in.read();
        } catch (IOException e) {
            throw new ConsoleLoggerException(e.getMessage(), e);
        }
    }

    public synchronized static void endMessage() {
        String output = "Bot work completed in " + computeTime(programStartTime.get());
        writeInLine("\n" + output);
        LOG.info(output);
    }

    static String computeTime(long startTime) {
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
