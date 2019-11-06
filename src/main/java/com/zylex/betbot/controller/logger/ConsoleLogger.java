package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

abstract class ConsoleLogger {

    static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    void writeLineSeparator() {
        writeInLine("\n" + StringUtils.repeat("-", 50));
    }

    void writeErrorMessage(String message) {
        System.err.print(message);
    }

    synchronized void writeInLine(String message) {
        System.out.print(message);
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
