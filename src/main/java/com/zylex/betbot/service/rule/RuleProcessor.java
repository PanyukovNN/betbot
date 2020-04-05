package com.zylex.betbot.service.rule;

import com.zylex.betbot.controller.logger.RuleProcessorLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.Rule;
import com.zylex.betbot.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.zylex.betbot.BetBotApplication.betStartTime;

/**
 * Filter games by rules.
 */
@Service
public class RuleProcessor {

    private RuleProcessorLogger logger = new RuleProcessorLogger();

    private GameRepository gameRepository;

    private RuleRepository ruleRepository;

    private RuleFilter ruleFilter;

    @Autowired
    public RuleProcessor(GameRepository gameRepository,
                         RuleRepository ruleRepository,
                         RuleFilter ruleFilter) {
        this.gameRepository = gameRepository;
        this.ruleRepository = ruleRepository;
        this.ruleFilter = ruleFilter;
    }

    /**
     * Filters games by specified rule, takes list of games from site or from database,
     * which depends on current time, sort games by time, then save eligible games to file,
     * and return games list.
     * @return - map of games lists by ruleNumbers.
     */
    @Transactional
    public Map<Rule, List<Game>> process(List<Game> games) {
        Map<Rule, List<Game>> ruleGames = new LinkedHashMap<>();
        List<Rule> rules = ruleRepository.getAll();
        rules.sort(Comparator.comparing(Rule::getId));
        for (Rule rule : rules) {
            List<Game> eligibleGames = ruleFilter.filter(games, rule);
            eligibleGames.sort(Comparator.comparing(Game::getDateTime));
            eligibleGames.forEach(gameRepository::save);
            ruleGames.put(rule, gameRepository.getSinceDateTime(LocalDateTime.of(LocalDate.now().minusDays(1), betStartTime)).stream()
                    .filter(game -> game.getRules().contains(rule))
                    .sorted(Comparator.comparing(Game::getDateTime))
                    .collect(Collectors.toList()));
        }
        logger.writeEligibleGamesNumber(ruleGames);
        return ruleGames;
    }
}
