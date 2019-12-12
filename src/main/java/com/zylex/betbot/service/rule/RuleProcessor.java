package com.zylex.betbot.service.rule;

import com.zylex.betbot.controller.repository.GameRepository;
import com.zylex.betbot.controller.repository.LeagueRepository;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.*;

/**
 * Filter games by rules.
 */
public class RuleProcessor {

    private ParseProcessor parseProcessor;

    private LeagueRepository leagueRepository;

    private GameRepository gameRepository;

    public RuleProcessor(GameRepository gameRepository, LeagueRepository leagueRepository, ParseProcessor parseProcessor) {
        this.gameRepository = gameRepository;
        this.leagueRepository = leagueRepository;
        this.parseProcessor = parseProcessor;
    }

    /**
     * Filters games by specified rule, takes list of games from site or from database,
     * which depends on current time, sort games by time, then save eligible games to file,
     * and return games list.
     * @return - map of games lists by ruleNumbers.
     */
    public Map<RuleNumber, List<Game>> process() {
        List<Game> games = parseProcessor.process();
        Map<RuleNumber, List<Game>> ruleGames = findRuleGames(games);
        ruleGames.forEach((ruleNumber, gameList) -> gameList.sort(Comparator.comparing(Game::getDateTime)));
        return ruleGames;
    }

    private Map<RuleNumber, List<Game>> findRuleGames(List<Game> games) {
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            eligibleGames.put(ruleNumber, ruleNumber.rule.filter(leagueRepository, games));
            gameRepository.saveByRule(ruleNumber, eligibleGames.get(ruleNumber));
        }
        return eligibleGames;
    }
}
