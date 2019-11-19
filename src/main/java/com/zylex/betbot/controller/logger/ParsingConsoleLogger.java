package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logs Parsing processor
 */
public class ParsingConsoleLogger extends ConsoleLogger {

    private AtomicInteger totalGames = new AtomicInteger(0);

    private int totalLeagues = 0;

    private AtomicInteger processedLeagues = new AtomicInteger(0);

    public void addTotalGames(int number) {
        totalGames.addAndGet(number);
    }

    /**
     * Log start messages.
     * @param type - type of log.
     * @param arg - different argument for specified log type.
     */
    public synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_SITE_START) {
            writeInLine("Start parsing.");
            writeInLine("\nFinding leagues: ...");
        } else if (type == LogType.GAMES) {
            totalLeagues = arg;
            writeInLine(String.format("\nProcessing games: 0/%d (0.0%%)", arg));
        }
    }

    /**
     * Log league finding.
     */
    public void logLeague() {
        String output = "Finding leagues: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
    }

    /**
     * Log count of processed games.
     */
    public synchronized void logLeagueGame() {
        String output = String.format("Processing leagues: %d/%d (%s%%)",
                processedLeagues.incrementAndGet(),
                totalLeagues,
                new DecimalFormat("#0.0").format(((double) processedLeagues.get() / (double) totalLeagues) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedLeagues.get() == totalLeagues) {
            writeLineSeparator();
        }
    }

    /**
     * Log summarizing of parsing.
     */
    public void parsingSummarizing() {
        writeInLine(String.format("\nTotal games: %d", totalGames.get()));
        writeInLine(String.format("\nParsing completed in %s", computeTime(programStartTime.get())));
        writeLineSeparator();
    }

    /**
     * Log number of eligible games for every rule.
     * @param eligibleGames - map of eligible games.
     */
    public void writeEligibleGamesNumber(Map<Day, Map<RuleNumber, List<Game>>> eligibleGames) {
        for (Day day : eligibleGames.keySet()) {
            for (RuleNumber ruleNumber : RuleNumber.values()) {
                writeInLine(String.format("\nEligible %s games for %s: %d", day, ruleNumber, eligibleGames.get(day).get(ruleNumber).size()));
            }
        }
        writeLineSeparator();
    }
}
