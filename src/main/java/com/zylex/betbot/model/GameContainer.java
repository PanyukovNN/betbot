package com.zylex.betbot.model;

import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Instance of container for all games lists.
 */
public class GameContainer {

    private LocalDateTime parsingTime;

    private Map<RuleNumber, List<Game>> eligibleGames;

    public GameContainer(LocalDateTime parsingTime, Map<RuleNumber, List<Game>> eligibleGames) {
        this.parsingTime = parsingTime;
        this.eligibleGames = eligibleGames;
    }

    public LocalDateTime getParsingTime() {
        return parsingTime;
    }

    public Map<RuleNumber, List<Game>> getEligibleGames() {
        return eligibleGames;
    }
}
