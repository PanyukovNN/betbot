package com.zylex.betbot.exception;

public class ConsoleLoggerException extends OneXBetBotException {

    public ConsoleLoggerException() {
    }

    public ConsoleLoggerException(String message) {
        super(message);
    }

    public ConsoleLoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
