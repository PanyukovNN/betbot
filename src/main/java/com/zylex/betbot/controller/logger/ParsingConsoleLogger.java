package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ParsingConsoleLogger extends ConsoleLogger {

    private AtomicInteger totalGames = new AtomicInteger(0);

    private int totalLeagues = 0;

    private AtomicInteger processedLeagues = new AtomicInteger(0);

    public void addTotalGames(int number) {
        totalGames.addAndGet(number);
    }

    public synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.PARSING_START) {
            Day day = arg == 0 ? Day.TODAY : Day.TOMORROW;
            writeInLine(String.format("Start parsing %s games.", day));
        } else if (type == LogType.LEAGUES) {
            writeInLine("\nFinding leagues: ...");
        } else if (type == LogType.GAMES) {
            totalLeagues = arg;
            writeInLine(String.format("\nProcessing games: 0/%d (0.0%%)", arg));
        }
    }

    public void logLeague() {
        String output = "Finding leagues: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
    }

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

    public void parsingSummarizing() {
        writeInLine(String.format("\nTotal games: %d", totalGames.get()));
        writeInLine(String.format("\nParsing completed in %s", computeTime(programStartTime.get())));
        writeLineSeparator();
    }

    public void writeEligibleGamesNumber(Map<RuleNumber, List<Game>> eligibleGames) {
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            writeInLine(String.format("\nEligible games for rule %s: %d", ruleNumber, eligibleGames.get(ruleNumber).size()));
        }
        writeLineSeparator();
    }
}
