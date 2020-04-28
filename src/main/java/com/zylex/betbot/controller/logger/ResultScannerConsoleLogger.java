package com.zylex.betbot.controller.logger;

import com.zylex.betbot.service.statistics.ResultScanner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log ResultScanner.
 */
public class ResultScannerConsoleLogger extends ConsoleLogger{

    private final static Logger LOG = LoggerFactory.getLogger(ResultScanner.class);

    private int totalGames;

    private final AtomicInteger processedGames = new AtomicInteger();

    /**
     * Log start messages.
     */
    public void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_SITE_START) {
            String output = "Scanning game results started.";
            writeInLine("\n" + output);
            writeLineSeparator();
            writeInLine("\nScanning games: ...");
            LOG.info(output);
        } else if (type == LogType.GAMES) {
            totalGames = arg;
            writeInLine(String.format("\nScanning games: 0/%d (0.0%%)", arg));
        }
    }

    /**
     * Log single game.
     */
    public void logGame() {
        String output = String.format("Scanning games: %d/%d (%s%%)",
                processedGames.incrementAndGet(),
                totalGames,
                new DecimalFormat("#0.0").format(((double) processedGames.get() / (double) totalGames) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
    }

    /**
     * Log end message.
     */
    public void endLogMessage() {
        writeLineSeparator('~');
        LOG.info("Scanning complete.");
    }

    /**
     * Log "no games to scan" message.
     */
    public void noGamesLog() {
        String output = "No games to scan.";
        writeInLine(StringUtils.repeat("\b", "Scanning games: ...".length()) + output);
        LOG.info(output);
    }
}
