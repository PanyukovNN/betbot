package com.zylex.betbot.controller.logger;

import java.time.LocalDate;

public class StatisticsAnalyserConsoleLogger extends ConsoleLogger{

    public synchronized void startLogMessage(LocalDate startDate, LocalDate endDate) {
        writeInLine(String.format("Analyse statistics for period from %s to %s", startDate, endDate));
        writeLineSeparator();
    }

    public void logStatistics(int gamesNumber1, int firstWins1, int ties1, int secondWins1, int noResults1,
                              int gamesNumber2, int firstWins2, int ties2, int secondWins2, int noResults2) {
        writeInLine(String.format("\n%8s%4d | %-4d", "Total - ", gamesNumber1, gamesNumber2));
        writeInLine(String.format("\n%8s%4d | %-4d", "1X - ", firstWins1, firstWins2));
        writeInLine(String.format("\n%8s%4d | %-4d", " X - ", ties1, ties2));
        writeInLine(String.format("\n%8s%4d | %-4d", "X2 - ", secondWins1, secondWins2));
        if (noResults1 > 0 || noResults2 > 0) {
            writeInLine(String.format("\n%8s%4d | %-4d", "N/R - ", noResults1, noResults2));
        }
        writeLineSeparator();
    }
}
