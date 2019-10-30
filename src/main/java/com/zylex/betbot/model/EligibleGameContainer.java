package com.zylex.betbot.model;

import java.util.List;

public class EligibleGameContainer {

    private BetCoefficient betCoefficient;

    private List<Game> allGames;

    private List<Game> eligibleGames;

    public EligibleGameContainer(BetCoefficient betCoefficient, List<Game> allGames, List<Game> eligibleGames) {
        this.betCoefficient = betCoefficient;
        this.allGames = allGames;
        this.eligibleGames = eligibleGames;
    }

    public BetCoefficient getBetCoefficient() {
        return betCoefficient;
    }

    public List<Game> getAllGames() {
        return allGames;
    }

    public List<Game> getEligibleGames() {
        return eligibleGames;
    }

}
