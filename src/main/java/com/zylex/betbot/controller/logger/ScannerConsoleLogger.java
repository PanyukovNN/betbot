package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Logs ResultScanner.
 */
public class ScannerConsoleLogger extends ConsoleLogger{

    /**
     * Log start messages.
     * @param gamesNumber - number of games.
     */
    public void startLogMessage(int gamesNumber) {
        String output = gamesNumber == 0
                ? "\nNo games to scan."
                : "\nProcess scanning: ...";
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
