package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.GameRepository;
import com.zylex.betbot.controller.LeagueRepository;
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

    private boolean refresh;

    public RuleProcessor(GameRepository gameRepository, LeagueRepository leagueRepository, ParseProcessor parseProcessor, boolean refresh) {
        this.gameRepository = gameRepository;
        this.leagueRepository = leagueRepository;
        this.parseProcessor = parseProcessor;
        this.refresh = refresh;
    }

    public GameRepository getGameRepository() {
        return gameRepository;
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
        eligibleGames.put(RuleNumber.RULE_ONE, new FirstRule().filter(leagueRepository, games));
        eligibleGames.put(RuleNumber.RULE_TEST, new TestRule().filter(leagueRepository, games));
        return eligibleGames;
    }

    private Map<RuleNumber, List<Game>> refreshGamesByParsingTime(Map<RuleNumber, List<Game>> eligibleGames) {
        Map<RuleNumber, List<Game>> betGames = new HashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            betGames.put(ruleNumber, new ArrayList<>());
            List<Game> fileBetGames = gameRepository.readByRule(ruleNumber);
            for (Day day : Day.values()) {
                LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX),
                        LocalTime.of(23, 0));
                if (LocalDateTime.now().isBefore(startBetTime) || refresh) {
                    List<Game> dayBetGames = filterGamesByDay(eligibleGames.get(ruleNumber), day);
                    betGames.get(ruleNumber).addAll(dayBetGames);
                    fileBetGames = removeDayGames(fileBetGames, day);
                    fileBetGames.addAll(dayBetGames);
                } else {
                    betGames.get(ruleNumber).addAll(filterGamesByDay(fileBetGames, day));
                }
            }
            logger.writeEligibleGamesNumber(fileBetGames, ruleNumber);
            gameRepository.saveByRule(ruleNumber, fileBetGames);
        }
        return betGames;
    }

    private List<Game> removeDayGames(List<Game> fileBetGames, Day day) {
        return fileBetGames.stream()
                .filter(game -> !game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                .collect(Collectors.toList());
    }

    private List<Game> filterGamesByDay(List<Game> eligibleGames, Day day) {
        return eligibleGames.stream()
                .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                .collect(Collectors.toList());
    }
}
