package com.zylex.betbot.controller;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConsoleLogger {

    public static int eligibleGames = 0;

    private static int totalLeagues = 0;

    private static AtomicInteger totalGames = new AtomicInteger(0);

    private static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    private static AtomicInteger processedLeagues = new AtomicInteger(0);

    private static int threads;

    private static int processedDrivers = 0;

    public static void addTotalGames(int totalGames) {
        ConsoleLogger.totalGames.addAndGet(totalGames);
    }

    public static synchronized void startLogMessage(LogType type, Integer arg) {
        if (type == LogType.DRIVERS) {
            threads = arg;
            writeInLine("Starting chrome drivers: 0/" + arg);
        } else if (type == LogType.LEAGUES) {
            writeInLine("\nProcessing leagues: ...");
        } else if (type == LogType.GAMES) {
            totalLeagues = arg;
            writeInLine(String.format("\nProcessing games: 0/%d (0.0%%)",
                    arg));
        } else if (type == LogType.BET) {
            threads = 0;
        }
    }

    public static synchronized void logDriver() {
        String output = String.format("Starting chrome drivers: %d/%d",
                ++processedDrivers,
                threads);
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedDrivers == threads) {
            writeLineSeparator();
        }
    }

    public static void logLeague() {
        String output = "Processing leagues: complete";
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
    }

    public static synchronized void logLeagueGame() {
        String output = String.format("Processing games: %d/%d (%s%%)",
                processedLeagues.incrementAndGet(),
                totalLeagues,
                new DecimalFormat("#0.0").format(((double) processedLeagues.get() / (double) totalLeagues) * 100).replace(",", "."));
        writeInLine(StringUtils.repeat("\b", output.length()) + output);
        if (processedLeagues.get() == totalLeagues) {
            writeLineSeparator();
        }
    }

    public static void logBet(int index, int singleBetAmount, BetCoefficient betCoefficient, Game game) {
        writeInLine(String.format("\n%d) %s rub. bet has been placed on %s for: %s",
                index,
                singleBetAmount,
                betCoefficient,
                game));
    }

    public static void parsingSummarizing() {
        writeInLine(String.format("\nTotal games: %d", totalGames.get()));
        writeInLine(String.format("\nParsing completed in %s", computeTime(programStartTime.get())));
        writeLineSeparator();
    }

    private static String computeTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - startTime) / 1000;
        long minutes = seconds / 60;
        long houres = 0;
        if (minutes > 60) {
            houres = minutes / 60;
            minutes = minutes % 60;
        }
        return (houres == 0 ? "" : houres + "h. ")
                + minutes + " min. "
                + seconds % 60 + " sec.";
    }

    public static void writeEligibleGamesNumber(int size) {
        eligibleGames = size;
        writeInLine("\nEligible games: " + eligibleGames);
    }

    public static void writeLineSeparator() {
        writeInLine("\n" + StringUtils.repeat("-", 50));
    }

    public static void writeErrorMessage(String message) {
        System.err.println(message);
    }

    public static synchronized void writeInLine(String message) {
        System.out.print(message);
    }
}
