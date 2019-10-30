package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleProcessor {

    private ParseProcessor parseProcessor;

    public RuleProcessor(ParseProcessor parseProcessor) {
        this.parseProcessor = parseProcessor;
    }

    public EligibleGameContainer process() {
        List<Game> games = parseProcessor.process(false);
        Map<BetCoefficient, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(BetCoefficient.FIRST_WIN, new FirstWinSecretRule().filter(games));
        eligibleGames.put(BetCoefficient.ONE_X, new OneXSecretRule().filter(games));
        return new EligibleGameContainer(games, eligibleGames);
    }
}
