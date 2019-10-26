package com.zylex.betbot.exception;

public class SaverException extends OneXBetParserException {

    public SaverException() {
    }

    public SaverException(String message) {
        super(message);
    }

    public SaverException(String message, Throwable cause) {
        super(message, cause);
    }
}
