package com.zylex.betbot.model;

import java.util.List;
import java.util.Map;

public class EligibleGameContainer {

    private List<Game> allGames;

    private Map<BetCoefficient, List<Game>> eligibleGames;

    public EligibleGameContainer(List<Game> allGames, Map<BetCoefficient, List<Game>> eligibleGames) {
        this.allGames = allGames;
        this.eligibleGames = eligibleGames;
    }

    public List<Game> getAllGames() {
        return allGames;
    }

    public Map<BetCoefficient, List<Game>> getEligibleGames() {
        return eligibleGames;
    }
}
