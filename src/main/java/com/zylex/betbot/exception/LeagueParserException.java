package com.zylex.betbot.exception;

public class LeagueParserException extends BetBotException {

    public LeagueParserException() {
    }

    public LeagueParserException(String message) {
        super(message);
    }

    public LeagueParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
