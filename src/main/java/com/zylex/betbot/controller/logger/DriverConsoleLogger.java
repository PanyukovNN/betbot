package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Logs DriverManager.
 */
public class DriverConsoleLogger extends ConsoleLogger {

    /**
     * Log start message.
     */
    public void startLogMessage() {
        writeInLine("\nStarting chrome driver: ...");
    }

    /**
     * Log driver start.
     */
    public void logDriver() {
        String output = "Starting chrome driver: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        writeLineSeparator();
    }
}
