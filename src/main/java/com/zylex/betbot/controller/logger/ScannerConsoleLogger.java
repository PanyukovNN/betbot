package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Logs ResultScanner.
 */
public class ScannerConsoleLogger extends ConsoleLogger{

    /**
     * Log start messages.
     */
    public void startLogMessage() {
        String output = "\nProcess scanning: ...";
        writeInLine(output);
    }

    /**
     * Log end message.
     * @param type - type of log.
     */
    public void endMessage(LogType type) {
        if (type == LogType.OK) {
            String output = "Process scanning: complete";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
        } else if (type == LogType.NO_GAMES_TO_SCAN) {
            writeInLine("\nNo games to scan");
        }
        writeLineSeparator();
    }
}
