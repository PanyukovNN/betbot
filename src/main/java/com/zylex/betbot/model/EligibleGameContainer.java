package com.zylex.betbot.model;

import java.util.List;

public class EligibleGameContainer {

    private BetCoefficient betCoefficient;

    private List<Game> eligibleGames;

    public EligibleGameContainer(BetCoefficient betCoefficient, List<Game> eligibleGames) {
        this.betCoefficient = betCoefficient;
        this.eligibleGames = eligibleGames;
    }

    public BetCoefficient getBetCoefficient() {
        return betCoefficient;
    }

    public List<Game> getEligibleGames() {
        return eligibleGames;
    }

}
