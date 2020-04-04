package com.zylex.betbot.service.rule;

import com.zylex.betbot.controller.logger.RuleProcessorLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.Rule;
import com.zylex.betbot.service.Day;
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

    private LeagueRepository leagueRepository;

    private GameRepository gameRepository;

    private BetInfoRepository betInfoRepository;

    private RuleRepository ruleRepository;

    @Autowired
    public RuleProcessor(LeagueRepository leagueRepository,
                         GameRepository gameRepository,
                         BetInfoRepository betInfoRepository,
                         RuleRepository ruleRepository) {
        this.leagueRepository = leagueRepository;
        this.gameRepository = gameRepository;
        this.betInfoRepository = betInfoRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Filters games by specified rule, takes list of games from site or from database,
     * which depends on current time, sort games by time, then save eligible games to file,
     * and return games list.
     * @return - map of games lists by ruleNumbers.
     */
    @Transactional
    public Map<RuleNumber, List<Game>> process(List<Game> games) {
        Map<RuleNumber, List<Game>> ruleGames = findRuleGames(games);
        return refreshByDay(ruleGames);
    }

    private Map<RuleNumber, List<Game>> findRuleGames(List<Game> games) {
        Map<RuleNumber, List<Game>> ruleGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            Rule rule = ruleRepository.getByRuleNumber(ruleNumber);
            List<Game> eligibleGames = ruleNumber.ruleFilter.filter(leagueRepository, games, rule);
            eligibleGames.sort(Comparator.comparing(Game::getDateTime));
            for (Game game : eligibleGames) {
                eligibleGames.set(eligibleGames.indexOf(game), gameRepository.save(game));
            }
            ruleGames.put(ruleNumber, eligibleGames);
        }
        return ruleGames;
    }

    private Map<RuleNumber, List<Game>> refreshByDay(Map<RuleNumber, List<Game>> ruleGames) {
        LocalDateTime betTime = betInfoRepository.getLast().getDateTime();
        Map<RuleNumber, List<Game>> betGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            betGames.put(ruleNumber, new ArrayList<>());
            for (Day day : Day.values()) {
                if (betTime.isAfter(LocalDateTime.of(LocalDate.now().plusDays(day.INDEX).minusDays(1), betStartTime.minusMinutes(1)))) {
                    List<Game> dayRuleGames = ruleGames.get(ruleNumber).stream()
                            .filter(game -> game.getDateTime().toLocalDate().equals(LocalDate.now().plusDays(day.INDEX)))
                            .sorted(Comparator.comparing(Game::getDateTime))
                            .collect(Collectors.toList());
                    betGames.get(ruleNumber).addAll(dayRuleGames);
                }
            }
        }
        logger.writeEligibleGamesNumber(ruleGames);
        return betGames;
    }
}
