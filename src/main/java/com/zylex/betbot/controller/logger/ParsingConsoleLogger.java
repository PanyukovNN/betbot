package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.parsing.ParseProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log ParseProcessor.
 */
public class ParsingConsoleLogger extends ConsoleLogger {

    private final static Logger LOG = LoggerFactory.getLogger(ParseProcessor.class);

    private static int totalLeagues;

    private static AtomicInteger processedLeagues = new AtomicInteger();

    /**
     * Log start messages.
     * @param type - type of log.
     * @param arg - different argument for specified log type.
     */
    public static synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_SITE_START) {
            String output = "Parsing started.";
            writeInLine("\n" + output);
            LOG.info(output);
            writeInLine("\nFinding leagues: ...");
        } else if (type == LogType.LEAGUES) {
            totalLeagues = arg;
            writeInLine(String.format("\nProcessing leagues: 0/%d (0.0%%)", arg));
        }
    }

    /**
     * Log league finding.
     */
    public static void logLeague() {
        String output = "Finding leagues: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        LOG.info("Finding leagues complete");
    }

    /**
     * Log count of processed games.
     */
    public static synchronized void logLeagueGame() {
        String output = String.format("Processing leagues: %d/%d (%s%%)",
                processedLeagues.incrementAndGet(),
                totalLeagues,
                new DecimalFormat("#0.0").format(((double) processedLeagues.get() / (double) totalLeagues) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedLeagues.get() == totalLeagues) {
            String parsingCompleteOutput = "Parsing completed in " + computeTime(programStartTime.get());
            writeInLine("\n" + parsingCompleteOutput);
            writeLineSeparator();
            LOG.info(parsingCompleteOutput);
        }
    }

    /**
     * Log summarizing of parsing.
     */
    public static void writeTotalGames(List<Game> games) {
        StringBuilder output = new StringBuilder("Total games - ");
        for (Day day : Day.values()) {
            int gamesCount = (int) games.stream()
                    .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                    .count();
            output.append(String.format("%3d (%s) ", gamesCount, day));
        }
        writeInLine("\n" + output);
        LOG.info(output.toString());
    }
}
