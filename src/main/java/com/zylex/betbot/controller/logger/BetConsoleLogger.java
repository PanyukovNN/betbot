package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.bet.Bet;
import com.zylex.betbot.model.bet.BetStatus;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.service.bet.BetProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log BetProcessor.
 */
public class BetConsoleLogger extends ConsoleLogger{

    private final static Logger LOG = LoggerFactory.getLogger(BetProcessor.class);

    private AtomicInteger gameIndex = new AtomicInteger(0);

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
     * @param game - game for bet.
     * @param gameBet - list of game bets.
     */
    public void logBet(Game game, List<Bet> gameBet) {
        String gameMessage = String.format("\n%3s %s", gameIndex.incrementAndGet() + ")", game);
        writeInLine(gameMessage);
        LOG.info(gameMessage);
        for (Bet bet : gameBet) {
            if (bet.getStatus().equals(BetStatus.SUCCESS.toString())) {
                String output = String.format("  - %s rub. placed on %-10s",
                        bet.getAmount(),
                        bet.getCoefficient());
                writeInLine("\n" + output);
                LOG.info(output);
            } else if (bet.getStatus().equals(BetStatus.FAIL.toString())) {
                String output = "   - Did't find the game";
                writeErrorMessage("\n" + output);
                LOG.warn(output);
            } else if (bet.getStatus().equals(BetStatus.ERROR.toString())) {
                String output = "   - Error during making bet: " + bet;
                writeErrorMessage("\n" + output);
                LOG.warn(output);
            }
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
