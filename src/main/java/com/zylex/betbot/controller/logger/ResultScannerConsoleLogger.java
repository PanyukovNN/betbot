package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

public class ResultScannerConsoleLogger extends ConsoleLogger{

    public void startLogMessage(int gamesNumber) {
        if (gamesNumber == 0) {
            writeInLine("\nNo games to scan.");
        } else {
            writeInLine("\nProcess scanning: ...");
        }
    }

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
