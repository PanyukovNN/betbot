package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import org.apache.commons.lang3.StringUtils;

/**
 * Logs BetProcessor.
 */
public class BetConsoleLogger extends ConsoleLogger{

    /**
     * Log start message.
     * @param type - type of log.
     * @param message - string message.
     */
    public synchronized void startLogMessage(LogType type, String message) {
        if (type == LogType.BET) {
            writeInLine(String.format("\nProcessing bets for %s:", message));
        } else if (type == LogType.LOG_IN) {
            writeLineSeparator();
            writeInLine("\nLogging in: ...");
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
            writeInLine(String.format("\n%d) %s rub. bet has been placed on %s for: %s",
                    index,
                    betAmount,
                    betCoefficient,
                    game));
        } else if (type == LogType.BET_NOT_FOUND) {
            writeErrorMessage("Did't find the game: " + game);
        } else if (type == LogType.BET_ERROR) {
            writeErrorMessage("Error during bet making: " + game);
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
        } else if (type == LogType.VERIFY) {
            String output = "Logging in: need to verify";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
            writeInLine("\nPlease, finish the verification in browser, after that press Enter to continue...");
            pressEnter();
        }
        writeLineSeparator();
    }

    /**
     * Log no money situation.
     */
    public void noMoney() {
        writeInLine("\nMoney is over.");
    }

    /**
     * Log end of bet making.
     * @param type - type of log.
     */
    public void betMade(LogType type) {
        if (type == LogType.OK) {
            writeLineSeparator();
            writeInLine("\nBets are made successfully.");
        } else if (type == LogType.ERROR) {
            writeInLine("\nBets aren't made.");
        }
        writeLineSeparator();
        writeInLine(String.format("\nBot work completed in %s", computeTime(programStartTime.get())));
    }
}
