package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.BetProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log BetProcessor.
 */
public class BetConsoleLogger extends ConsoleLogger{

    private final static Logger LOG = LoggerFactory.getLogger(BetProcessor.class);

    /**
     * Log start message.
     * @param type - type of log.
     * @param message - string message.
     */
    public synchronized void startLogMessage(LogType type, String message) {
        if (type == LogType.BET) {
            writeInLine(String.format("\nProcessing bets for %s:", message));
            LOG.info("Processing bets started");
        } else if (type == LogType.LOG_IN) {
            writeInLine("\nLogging in: ...");
            LOG.info("Logging in started");
        }
    }

    /**
     * Log single bet.
     * @param index - number of bet.
     * @param betAmount - amount of bet.
     * @param betCoefficient - coefficient of bet.
     * @param game - game for bet.
     * @param type - type of log.
     */
    public void logBet(int index, int betAmount, BetCoefficient betCoefficient, Game game, LogType type) {
        if (type == LogType.OK) {
            String output = String.format("%d) %s rub. bet has been placed on %s for: %s",
                    index,
                    betAmount,
                    betCoefficient,
                    game);
            writeInLine("\n" + output);
            LOG.info(output);
        } else if (type == LogType.BET_NOT_FOUND) {
            String output = String.format("%d) Did't find the game: %s", index, game);
            writeErrorMessage("\n" + output, new Throwable());
            LOG.warn(output);
        } else if (type == LogType.BET_ERROR) {
            String output = "Error during bet making for game: " + game;
            writeErrorMessage("\n" + output, new Throwable());
            LOG.warn(output);
        }
    }

    /**
     * Log of site log in.
     * @param type - type of log.
     */
    public void logInLog(LogType type) {
        if (type == LogType.OK) {
            String output = "Logging in: complete";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
            LOG.info("Logging in complete");
        } else if (type == LogType.VERIFY) {
            String output = "Logging in: need to verify";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
            writeInLine("\nPlease, finish the verification in browser, after that press Enter to continue...");
            LOG.info("Logging in need verify");
            pressAnyButton();
            LOG.info("Logging in verify finished");
        }
        writeLineSeparator();
    }

    /**
     * Log no money situation.
     */
    public void noMoney() {
        String output = "Money is over.";
        writeInLine("\n" + output);
        LOG.info(output);
    }

    /**
     * Log end of bet making.
     * @param type - type of log.
     */
    public void betMade(LogType type) {
        if (type == LogType.OK) {
            writeLineSeparator();
            String output = "Bets are made successfully.";
            writeInLine("\n" + output);
            LOG.info(output);
        } else if (type == LogType.NO_GAMES_TO_BET) {
            String output = "No appropriate betting games.";
            writeInLine("\n" + output);
            LOG.info(output);
        }
        writeLineSeparator('~');
    }
}
