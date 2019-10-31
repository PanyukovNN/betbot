package com.zylex.betbot.controller;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConsoleLogger {

    private static int totalLeagues = 0;

    private static AtomicInteger totalGames = new AtomicInteger(0);

    private static AtomicLong programStartTime = new AtomicLong(System.currentTimeMillis());

    private static AtomicInteger processedLeagues = new AtomicInteger(0);

    private static int threads;

    private static int processedDrivers = 0;

    public static void addTotalGames(int number) {
        totalGames.addAndGet(number);
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
            writeInLine("\nProcessing bets:");
        } else if (type == LogType.LOG_IN) {
            writeLineSeparator();
            writeInLine("\nLogging in: ...");
        } else if (type == LogType.LOG_OUT) {
            writeLineSeparator();
            writeInLine("\nLogging out: ...");
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

    public static void logBet(int index, int singleBetAmount, BetCoefficient betCoefficient, Game game, LogType type) {
        if (type == LogType.OK) {
            writeInLine(String.format("\n%d) %s rub. bet has been placed on %s for: %s",
                    index,
                    singleBetAmount,
                    betCoefficient,
                    game));
        } else if (type == LogType.ERROR) {
            writeErrorMessage("Did't find the game: " + game);
        }
    }

    public static void noMoney() {
        writeInLine("\nMoney is over.");
    }

    public static void logRule(RuleNumber ruleNumber) {
        writeInLine("\nUsing rule number: " + ruleNumber);
    }

    public static void logInLog(LogType type) {
        if (type == LogType.OK) {
            writeInLine(StringUtils.repeat("\b", 18) + "Logging in: complete");
            writeLineSeparator();
        } else if (type == LogType.ERROR) {
            writeErrorMessage("\nError: problem with authorization, need to verify.");
        }
    }

    public static void logOutLog(LogType type) {
        if (type == LogType.OK) {
            String output = "Logging out: complete\n";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
        } else if (type == LogType.ERROR) {
            String output = "Logging out: error (program terminated)\n";
            writeErrorMessage(StringUtils.repeat("\b", output.length()) + output);
        }
    }

    public static void parsingSummarizing() {
        writeInLine(String.format("\nTotal games: %d", totalGames.get()));
        writeInLine(String.format("\nParsing completed in %s", computeTime(programStartTime.get())));
        writeLineSeparator();
        processedDrivers = 0;
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

    public static void writeEligibleGamesNumber(Map<RuleNumber, List<Game>> eligibleGames) {
        for (Map.Entry<RuleNumber, List<Game>> entry : eligibleGames.entrySet()) {
            writeInLine(String.format("\nEligible games %s: %d", entry.getKey(), entry.getValue().size()));
        }
        writeLineSeparator();
    }

    private static void writeLineSeparator() {
        writeInLine("\n" + StringUtils.repeat("-", 50));
    }

    private static void writeErrorMessage(String message) {
        System.err.print(message);
    }

    private static synchronized void writeInLine(String message) {
        System.out.print(message);
    }

    public static void betsMade(LogType type) {
        if (type == LogType.OK) {
            writeInLine("\nBets are made successfully.");
        } else if (type == LogType.ERROR) {
            writeInLine("\nBets aren't made.");
        }
    }
}
