package com.zylex.betbot.exception;

public class BetRepositoryException extends OneXBetBotException {

    public BetRepositoryException() {
    }

    public BetRepositoryException(String message) {
        super(message);
    }

    public BetRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
