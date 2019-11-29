package com.zylex.betbot.exception;

import com.zylex.betbot.controller.logger.ConsoleLogger;

public class BetBotException extends RuntimeException {

    public BetBotException() {
    }

    public BetBotException(String message) {
        super(message);
    }

    public BetBotException(String message, Throwable cause) {
        super(message, cause);
        ConsoleLogger.writeExceptionToLog(message);
    }
}
