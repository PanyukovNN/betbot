package com.zylex.betbot.controller.logger;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logs ResultScanner.
 */
public class ResultScannerConsoleLogger extends ConsoleLogger{

    private int totalGames;

    private AtomicInteger processedGames = new AtomicInteger();

    /**
     * Log start messages.
     */
    public void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_SITE_START) {
            writeInLine("Start scanning game results.");
        } else if (type == LogType.GAMES) {
            totalGames = arg;
            writeInLine(String.format("\nProcessing games: 0/%d (0.0%%)", arg));
        }
    }

    /**
     * Log single game. If all games logged - print end message.
     */
    public void logGame() {
        String output = String.format("Processing games: %d/%d (%s%%)",
                processedGames.incrementAndGet(),
                totalGames,
                new DecimalFormat("#0.0").format(((double) processedGames.get() / (double) totalGames) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedGames.get() == totalGames) {
            writeInLine(String.format("\nScanning completed in %s", computeTime(programStartTime.get())));
            writeLineSeparator();
        }
    }

    /**
     * Log "no games to scan" message.
     */
    public void noGamesLog() {
        writeLineSeparator();
        writeInLine("\nNo games to scan");
        writeLineSeparator();
    }
}
