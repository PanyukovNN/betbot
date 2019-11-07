package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

public class DriverConsoleLogger extends ConsoleLogger {

    public synchronized void startLogMessage(LogType type) {
        if (type == LogType.DRIVER) {
            writeInLine("\nStarting chrome driver: ...");
        }
    }

    public synchronized void logDriver() {
        String output = "Starting chrome driver: complete";
        writeInLine(StringUtils.repeat("\b", "\nStarting chrome driver: ...".length()) + output);
        writeLineSeparator();
    }
}
