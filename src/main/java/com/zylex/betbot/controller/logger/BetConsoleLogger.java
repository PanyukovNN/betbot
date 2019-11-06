package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

public class BetConsoleLogger extends ConsoleLogger{

    public synchronized void startLogMessage(LogType type) {
        if (type == LogType.BET) {
            writeInLine("\nProcessing bets:");
        } else if (type == LogType.LOG_IN) {
            writeLineSeparator();
            writeInLine("\nLogging in: ...");
        } else if (type == LogType.LOG_OUT) {
            writeLineSeparator();
            writeInLine("\nLogging out: ...");
        }
    }

    public void logRule(RuleNumber ruleNumber) {
        writeInLine("\nUsing: " + ruleNumber);
    }

    public void logBet(int index, int singleBetAmount, BetCoefficient betCoefficient, Game game, LogType type) {
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

    public void logInLog(LogType type) {
        if (type == LogType.OK) {
            String output = "Logging in: complete";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
            writeLineSeparator();
        } else if (type == LogType.ERROR) {
            writeErrorMessage("\nError: problem with authorization, need to verify.");
        }
    }

    public void logOutLog(LogType type) {
        if (type == LogType.OK) {
            String output = "Logging out: complete\n";
            writeInLine(StringUtils.repeat("\b", output.length()) + output);
        } else if (type == LogType.ERROR) {
            String output = "Logging out: error (program terminated)\n";
            writeErrorMessage(StringUtils.repeat("\b", output.length()) + output);
        }
    }

    public void noMoney() {
        writeInLine("\nMoney is over.");
    }

    public void betsMade(LogType type) {
        if (type == LogType.OK) {
            writeLineSeparator();
            writeInLine("\nBets are made successfully.");
        } else if (type == LogType.ERROR) {
            writeInLine("\nBets aren't made.");
        }
        writeLineSeparator();
    }
}
