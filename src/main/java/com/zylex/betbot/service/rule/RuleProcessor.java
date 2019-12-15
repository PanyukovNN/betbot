package com.zylex.betbot.service.rule;

import com.zylex.betbot.controller.GameDao;
import com.zylex.betbot.controller.repository.BetInfoRepository;
import com.zylex.betbot.controller.repository.GameRepository;
import com.zylex.betbot.controller.repository.LeagueRepository;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Filter games by rules.
 */
public class RuleProcessor {

    private ParseProcessor parseProcessor;

    private LeagueRepository leagueRepository;

    private GameRepository gameRepository;

    private GameDao gameDao;

    private BetInfoRepository betInfoRepository;

    public RuleProcessor(GameRepository gameRepository, LeagueRepository leagueRepository, ParseProcessor parseProcessor, GameDao gameDao, BetInfoRepository betInfoRepository) {
        this.gameRepository = gameRepository;
        this.leagueRepository = leagueRepository;
        this.parseProcessor = parseProcessor;
        this.gameDao = gameDao;
        this.betInfoRepository = betInfoRepository;
    }

    public GameDao getGameDao() {
        return gameDao;
    }

    public BetInfoRepository getBetInfoRepository() {
        return betInfoRepository;
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
        Map<RuleNumber, List<Game>> betGames = refreshByDay(ruleGames);
        betGames.forEach((ruleNumber, gameList) -> gameList.sort(Comparator.comparing(Game::getDateTime)));
        return betGames;
    }

    private Map<RuleNumber, List<Game>> refreshByDay(Map<RuleNumber, List<Game>> ruleGames) {
        LocalDateTime betTime = betInfoRepository.read();
        Map<RuleNumber, List<Game>> betGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            for (Day day : Day.values()) {
                List<Game> dayGames = gameDao.getByDate(ruleNumber, LocalDate.now().plusDays(day.INDEX));
                if (betTime.isAfter(LocalDateTime.of(LocalDate.now().plusDays(day.INDEX).minusDays(1), LocalTime.of(22, 59)))) {
                    betGames.put(ruleNumber, dayGames);
                } else {
                    //TODO
                    betGames.put(ruleNumber, ruleGames.get(ruleNumber));
                    dayGames.forEach(gameDao::delete);
                    ruleGames.get(ruleNumber).forEach(game -> gameDao.save(game, ruleNumber));
                }
            }
        }
        return betGames;
    }

    private Map<RuleNumber, List<Game>> findRuleGames(List<Game> games) {
        Map<RuleNumber, List<Game>> ruleGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            ruleGames.put(ruleNumber, ruleNumber.rule.filter(leagueRepository, games));
        }
        return ruleGames;
    }
}
