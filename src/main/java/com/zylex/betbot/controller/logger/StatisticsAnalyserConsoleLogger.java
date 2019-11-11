package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

public class StatisticsAnalyserConsoleLogger extends ConsoleLogger{

    private int index = 0;

    public synchronized void startLogMessage(LocalDate startDate, LocalDate endDate) {
        writeInLine(String.format("Analyse statistics for period from %s to %s", startDate, endDate));
    }

    public void logStatistics(String message, int gamesNumber, int firstWins, int ties, int secondWins, int noResults) {
        writeInLine("\n" + message + ":");
        writeInLine(" games number: " + gamesNumber);
        if (noResults > 0) {
            writeInLine(String.format("; No result: %d", noResults));
        }
        writeInLine(String.format("\n1X: %d\n", firstWins));
        writeInLine(String.format(" X: %d\n", ties));
        writeInLine(String.format("X2: %d\n", secondWins));
    }
}
