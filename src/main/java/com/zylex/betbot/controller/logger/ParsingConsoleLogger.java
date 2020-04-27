package com.zylex.betbot.controller.logger;

import com.zylex.betbot.service.parsing.ParseProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log ParseProcessor.
 */
public class ParsingConsoleLogger extends ConsoleLogger {

    private final static Logger LOG = LoggerFactory.getLogger(ParseProcessor.class);

    private static int totalLeagues;

    private final static AtomicInteger processedLeagues = new AtomicInteger();

    /**
     * Log start messages.
     * @param type - type of log.
     * @param arg - different argument for specified log type.
     */
    public static synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_SITE_START) {
            writeInLine("\nFinding leagues: ...");
            LOG.info("Finding leagues started.");
        } else if (type == LogType.LEAGUES) {
            totalLeagues = arg;
            writeInLine(String.format("\nParsing leagues: 0/%d (0.0%%)", arg));
        }
    }

    /**
     * Log league finding.
     */
    public static void logLeague() {
        String output = "Finding leagues: complete.";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        LOG.info(output);
    }

    /**
     * Log count of processed games.
     */
    public static synchronized void logLeagueGame() {
        String output = String.format("Parsing leagues: %d/%d (%s%%)",
                processedLeagues.incrementAndGet(),
                totalLeagues,
                new DecimalFormat("#0.0").format(((double) processedLeagues.get() / (double) totalLeagues) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedLeagues.get() == totalLeagues) {
            writeLineSeparator();
            LOG.info("Parsing completed.");
        }
    }
}
