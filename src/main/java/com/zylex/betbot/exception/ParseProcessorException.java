package com.zylex.betbot.exception;

public class ParseProcessorException extends OneXBetParserException {

    public ParseProcessorException() {
    }

    public ParseProcessorException(String message) {
        super(message);
    }

    public ParseProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
