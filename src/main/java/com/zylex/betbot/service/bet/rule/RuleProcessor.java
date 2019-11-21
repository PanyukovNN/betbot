package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.RuleProcessorException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filters games by rules.
 */
public class RuleProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ParseProcessor parseProcessor;

    private boolean refresh;

    private Repository repository;

    private RuleNumber ruleNumber;

    public RuleProcessor(Repository repository, ParseProcessor parseProcessor, RuleNumber ruleNumber, boolean refresh) {
        this.repository = repository;
        this.parseProcessor = parseProcessor;
        this.ruleNumber = ruleNumber;
        this.refresh = refresh;
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * Filters games by rules, puts in GameContainer and returns it.
     * @return - container of all lists of games.
     */
    public List<Game> process() {
        try {
            List<Game> games = parseProcessor.process();
            List<Game> eligibleGames = new FirstRule().filter(games);
            List<Game> fileBetGames = repository.readTotalRuleResultFile(ruleNumber);
            List<Game> betGames = new ArrayList<>();
            for (Day day : Day.values()) {
                LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX),
                        LocalTime.of(23, 0));
                if (LocalDateTime.now().isBefore(startBetTime) || refresh) {
                    List<Game> dayBetGames = filterGamesByDay(eligibleGames, day);
                    betGames.addAll(dayBetGames);
                    fileBetGames = removeDayGames(fileBetGames, day);
                    fileBetGames.addAll(dayBetGames);
                } else {
                    betGames.addAll(filterGamesByDay(fileBetGames, day));
                }
            }
            logger.writeEligibleGamesNumber(fileBetGames, ruleNumber);
            repository.saveTotalRuleResultFile(ruleNumber, fileBetGames);
            return betGames;
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private List<Game> removeDayGames(List<Game> fileBetGames, Day day) {
        fileBetGames = fileBetGames.stream()
                .filter(game -> !game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                .collect(Collectors.toList());
        return fileBetGames;
    }

    private List<Game> filterGamesByDay(List<Game> eligibleGames, Day day) {
        return eligibleGames.stream()
                .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                .collect(Collectors.toList());
    }
}
