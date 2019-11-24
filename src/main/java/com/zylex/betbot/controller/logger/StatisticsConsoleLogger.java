package com.zylex.betbot.controller.logger;

import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.time.LocalDate;

/**
 * Logs StatisticsAnalyser.
 */
public class StatisticsConsoleLogger extends ConsoleLogger{

    /**
     * Log start message.
     * @param startDate - start date.
     * @param endDate - end date.
     */
    public synchronized void startLogMessage(LocalDate startDate, LocalDate endDate) {
        writeInLine(String.format("Analyse statistics for period from %s to %s", startDate, endDate));
        writeLineSeparator();
    }

    /**
     * Log formatted statistics.
     * @param ruleNumber - number of rule.
     * @param gamesNumber1 - number of total games.
     * @param firstWins1 - number of total first win games.
     * @param ties1 - number of total tie games.
     * @param secondWins1 - number of total second win games.
     * @param noResults1 - number of total no result games.
     * @param gamesNumber2 - number of games for leagues from file.
     * @param firstWins2 - number of first win games for leagues from file.
     * @param ties2 - number of total tie games for leagues from file.
     * @param secondWins2 - number of total second win games for leagues from file.
     * @param noResults2 - number of total no result games for leagues from file.
     */
    public void logStatistics(RuleNumber ruleNumber,
                              int gamesNumber1, int firstWins1, int ties1, int secondWins1, int noResults1,
                              int gamesNumber2, int firstWins2, int ties2, int secondWins2, int noResults2) {
        writeInLine("\nGames for " + ruleNumber + ":");
        writeInLine(String.format("\n%10s%4d | %-4d", "Total - ", gamesNumber1, gamesNumber2));
        writeInLine(String.format("\n%10s%4d | %-4d", "1P - ", firstWins1, firstWins2));
        writeInLine(String.format("\n%10s%4d | %-4d", " X - ", ties1, ties2));
        writeInLine(String.format("\n%10s%4d | %-4d", "P2 - ", secondWins1, secondWins2));
        if (noResults1 > 0 || noResults2 > 0) {
            writeInLine(String.format("\n%10s%4d | %-4d", "N/R - ", noResults1, noResults2));
        }
        writeLineSeparator();
    }
}
