package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters games by rules.
 */
public class RuleProcessor {

    private ParseProcessor parseProcessor;

    public RuleProcessor(ParseProcessor parseProcessor) {
        this.parseProcessor = parseProcessor;
    }

    /**
     * Filters games by all rules and puts filtered lists in GameContainer.
     * @return - container of all lists of games.
     */
    public GameContainer process() {
        List<Game> games = parseProcessor.process();
        Map<BetCoefficient, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(BetCoefficient.FIRST_WIN, new FirstWinSecretRule().filter(games));
        eligibleGames.put(BetCoefficient.ONE_X, new OneXSecretRule().filter(games));
        return new GameContainer(games, eligibleGames);
    }
}
