package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.OneXBetBot;
import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;

import java.util.List;

public class RuleSaver extends RuleBaseDecorator {

    public RuleSaver(Rule rule) {
        super(rule);
    }

    @Override
    public EligibleGameContainer filter(List<Game> games) {
        Repository repository = new Repository(OneXBetBot.day);
        repository.processSaving(games, "all_matches_");
        EligibleGameContainer gameContainer = super.filter(games);
        String eligibleFileName = String.format("eligible_matches_%s_", gameContainer.getBetCoefficient());
        repository.processSaving(gameContainer.getEligibleGames(), eligibleFileName);
        return gameContainer;
    }
}
