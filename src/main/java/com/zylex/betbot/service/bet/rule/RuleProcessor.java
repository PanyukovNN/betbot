package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.GameDao;
import com.zylex.betbot.controller.repository.GameRepository;
import com.zylex.betbot.controller.repository.LeagueRepository;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filter games by rules.
 */
public class RuleProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ParseProcessor parseProcessor;

    private GameRepository gameRepository;

    private LeagueRepository leagueRepository;

    private GameDao gameDao;

    public RuleProcessor(GameRepository gameRepository, LeagueRepository leagueRepository, ParseProcessor parseProcessor, GameDao gameDao) {
        this.gameRepository = gameRepository;
        this.leagueRepository = leagueRepository;
        this.parseProcessor = parseProcessor;
        this.gameDao = gameDao;
    }

    public GameDao getGameDao() {
        return gameDao;
    }

    /**
     * Filters games by specified rule, takes list of games from site or from file,
     * which depends on current time, sort games by time, then save eligible games to file,
     * and return games list.
     * @return - map of games lists by ruleNumbers.
     */
    public Map<RuleNumber, List<Game>> process() {
        List<Game> games = parseProcessor.process();
        Map<RuleNumber, List<Game>> eligibleGames = findEligibleGames(games);
        Map<RuleNumber, List<Game>> betGames = refreshGamesByParsingTime(eligibleGames);
        betGames.forEach((ruleNumber, gameList) -> gameList.sort(Comparator.comparing(Game::getDateTime)));
        return betGames;
    }

    private Map<RuleNumber, List<Game>> findEligibleGames(List<Game> games) {
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            eligibleGames.put(ruleNumber, ruleNumber.rule.filter(leagueRepository, games));
        }
        return eligibleGames;
    }

    private Map<RuleNumber, List<Game>> refreshGamesByParsingTime(Map<RuleNumber, List<Game>> eligibleGames) {
        Map<RuleNumber, List<Game>> betGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            betGames.put(ruleNumber, new ArrayList<>());
            for (Day day : Day.values()) {
                LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX),
                        LocalTime.of(23, 0));
                if (LocalDateTime.now().isBefore(startBetTime)) {
                    List<Game> dayBetGames = filterGamesByDay(eligibleGames.get(ruleNumber), day);
                    betGames.get(ruleNumber).addAll(dayBetGames);
                } else {
                    betGames.get(ruleNumber).addAll(gameDao.getByDate(ruleNumber, LocalDate.now().plusDays(day.INDEX)));
                }
            }
            gameRepository.saveByRule(ruleNumber, betGames.get(ruleNumber));
        }
        logger.writeEligibleGamesNumber(betGames);
        return betGames;
    }

    private List<Game> filterGamesByDay(List<Game> eligibleGames, Day day) {
        return eligibleGames.stream()
                .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                .collect(Collectors.toList());
    }
}
