package com.zylex.betbot.model;

import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.util.List;
import java.util.Map;

/**
 * Instance of container for all games lists.
 */
public class GameContainer {

    private List<Game> allGames;

    private Map<RuleNumber, List<Game>> eligibleGames;

    public GameContainer(List<Game> allGames, Map<RuleNumber, List<Game>> eligibleGames) {
        this.allGames = allGames;
        this.eligibleGames = eligibleGames;
    }

    public List<Game> getAllGames() {
        return allGames;
    }

    public Map<RuleNumber, List<Game>> getEligibleGames() {
        return eligibleGames;
    }
}
