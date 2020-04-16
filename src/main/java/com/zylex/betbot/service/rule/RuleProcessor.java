package com.zylex.betbot.service.rule;

import com.zylex.betbot.controller.logger.RuleProcessorLogger;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filter games by rules.
 */
@Service
public class RuleProcessor {

    private RuleProcessorLogger logger = new RuleProcessorLogger();

    private GameRepository gameRepository;

    private RuleRepository ruleRepository;

    private RuleFilter ruleFilter;

    private ParseProcessor parseProcessor;

    @Autowired
    public RuleProcessor(GameRepository gameRepository,
                         RuleRepository ruleRepository,
                         RuleFilter ruleFilter,
                         ParseProcessor parseProcessor) {
        this.gameRepository = gameRepository;
        this.ruleRepository = ruleRepository;
        this.ruleFilter = ruleFilter;
        this.parseProcessor = parseProcessor;
    }

    /**
     * Filters games by all rules, and set rule to filtered game.
     */
    @Transactional
    public void process() {
        List<Game> games = parseProcessor.process();
        List<Rule> rules = ruleRepository.getAll();
        for (Rule rule : rules) {
            List<Game> eligibleGames = ruleFilter.filter(games, rule);
            eligibleGames.sort(Comparator.comparing(Game::getDateTime));
            eligibleGames.forEach(gameRepository::save);
        }
        List<Rule> activatedRules = ruleRepository.getActivated();
        logger.writeEligibleGamesNumber(
                filterGamesByRules(gameRepository.getByBetStartTime(), activatedRules), activatedRules);
    }

    public List<Game> filterGamesByRules(List<Game> games, List<Rule> rules) {
        List<Game> filteredGames = new ArrayList<>();
        for (Rule rule : rules) {
            filteredGames.addAll(filterGamesByRule(games, rule));
        }
        return filteredGames;
    }

    public List<Game> filterGamesByRule(List<Game> games, Rule rule) {
        List<Game> ruleGames = games.stream()
                .filter(game -> game.getRules().contains(rule))
                .collect(Collectors.toList());
        return ruleFilter.filter(ruleGames, rule).stream()
                .sorted(Comparator.comparing(Game::getDateTime))
                .collect(Collectors.toList());
    }
}
