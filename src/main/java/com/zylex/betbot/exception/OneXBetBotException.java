package com.zylex.betbot.exception;

import com.zylex.betbot.controller.logger.ConsoleLogger;

public class OneXBetBotException extends RuntimeException {

    public OneXBetBotException() {
    }

    public OneXBetBotException(String message) {
        super(message);
    }

    public OneXBetBotException(String message, Throwable cause) {
        super(message, cause);
        ConsoleLogger.writeExceptionToLog(message);
    }
}
