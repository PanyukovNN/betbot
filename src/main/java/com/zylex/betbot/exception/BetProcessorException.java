package com.zylex.betbot.exception;

public class BetProcessorException extends BetBotException {

    public BetProcessorException() {
    }

    public BetProcessorException(String message) {
        super(message);
    }

    public BetProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
