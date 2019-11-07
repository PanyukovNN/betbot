package com.zylex.betbot.exception;

public class GameBotException extends OneXBetBotException {

    public GameBotException() {
    }

    public GameBotException(String message) {
        super(message);
    }

    public GameBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
