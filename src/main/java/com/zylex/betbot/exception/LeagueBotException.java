package com.zylex.betbot.exception;

public class LeagueBotException extends OneXBetBotException {

    public LeagueBotException() {
    }

    public LeagueBotException(String message) {
        super(message);
    }

    public LeagueBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
