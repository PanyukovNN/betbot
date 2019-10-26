package com.zylex.betbot.exception;

public class GameParserException extends OneXBetParserException {

    public GameParserException() {
    }

    public GameParserException(String message) {
        super(message);
    }

    public GameParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
