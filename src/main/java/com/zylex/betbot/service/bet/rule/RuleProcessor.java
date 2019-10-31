package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.ConsoleLogger;
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
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(RuleNumber.ONE, new FirstWinSecretRule().filter(games));
        eligibleGames.put(RuleNumber.TWO, new OneXSecretRule().filter(games));
        ConsoleLogger.writeEligibleGamesNumber(eligibleGames);
        return new GameContainer(games, eligibleGames);
    }
}
