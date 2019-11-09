package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import org.apache.commons.lang3.StringUtils;

public class ResultScannerConsoleLogger extends ConsoleLogger{

    private int index = 0;

    public synchronized void startLogMessage() {
        writeInLine("\nScanning results:");
    }

    public void logBetMadeGame(Game game) {
        writeInLine(String.format("\n%d) Using rule: %s, match result %s, for game: %s",
                ++index,
                StringUtils.join(game.getRuleNumberSet(), ", "),
                game.getGameResult(),
                game));
    }

    public void endMessage(LogType type) {
        if (type == LogType.OK) {
            writeInLine("\nResults are scanned.");
        } else if (type == LogType.NO_GAMES_TO_SCAN) {
            writeInLine("\nNo games to scan");
        }
        writeLineSeparator();
        writeInLine(String.format("\nBot work completed in %s", computeTime(programStartTime.get())));
    }
}
