package com.zylex.betbot.exception;

public class LeagueRepositoryException extends BetBotException {

    public LeagueRepositoryException() {
    }

    public LeagueRepositoryException(String message) {
        super(message);
    }

    public LeagueRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
