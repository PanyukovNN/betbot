package com.zylex.betbot.exception;

public class GameRepositoryException extends BetBotException {

    public GameRepositoryException() {
    }

    public GameRepositoryException(String message) {
        super(message);
    }

    public GameRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
