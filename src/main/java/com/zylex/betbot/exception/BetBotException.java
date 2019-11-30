package com.zylex.betbot.exception;

import com.zylex.betbot.controller.logger.ConsoleLogger;

@SuppressWarnings("WeakerAccess")
public class BetBotException extends RuntimeException {

    public BetBotException(String message, Throwable cause) {
        super(message, cause);
        ConsoleLogger.writeExceptionToLog(message);
    }
}
