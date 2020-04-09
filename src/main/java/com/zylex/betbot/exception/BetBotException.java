package com.zylex.betbot.exception;

import com.zylex.betbot.controller.logger.ConsoleLogger;

public class BetBotException extends RuntimeException {

    public BetBotException(String message) {
        super(message);
        ConsoleLogger.writeErrorMessage(message);
    }

    public BetBotException(String message, Throwable cause) {
        super(message, cause);
        ConsoleLogger.writeErrorMessage(message, cause);
    }
}
