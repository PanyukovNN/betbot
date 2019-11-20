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
            List<Game> games = parseProcessor.process(); // сегодняшние и завтрашние игры с сайта
            List<Game> eligibleGames = new FirstRule().filter(games);

            List<Game> fileBetGames = repository.readTotalRuleResultFile(ruleNumber);
            Map<Day, LocalDateTime> dayParsingTime = repository.readInfoFile();

            List<Game> betGames = new ArrayList<>();
            for (Day day : Day.values()) {
                LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX),
                        LocalTime.of(23, 0));
                LocalDateTime parsingTime = dayParsingTime.get(day);
                if (parsingTime.isBefore(startBetTime) || refresh) {
                    List<Game> dayBetGames = filterGamesByDay(eligibleGames, day);
                    betGames.addAll(dayBetGames);
                    fileBetGames = removeDayGames(fileBetGames, day);
                    fileBetGames.addAll(dayBetGames);
                    System.out.print("\n" + day + " games updated.");
                } else {
                    betGames.addAll(filterGamesByDay(fileBetGames, day));
                }
            }
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

    /*private List<Game> processDayGames() {
        List<Game> games = parseProcessor.process();
        List<Game> betGames = new ArrayList<>();
        //TODO if no games found for day
        for (Day day : Day.values()) {
            LocalDateTime parsingTime = games.stream()
                    .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                    .findAny()
                    .get()
                    .getParsingTime();
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX), LocalTime.of(23, 0));
            if (parsingTime.isBefore(startBetTime) || refresh) {
                betGames.addAll(games.stream()
                        .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                        .collect(Collectors.toList()));
                System.out.print("\n" + day + " games updated.");
            } else {
                //TODO fix ruleNumber
                betGames.addAll(repositoryFactory.getRepository(day).readRuleFile(RuleNumber.RULE_ONE));
            }
        }
        return betGames;
    }*/

    /*private GameContainer processGameContainerMap(Map<RuleNumber, List<Game>> eligibleGames) {
        LocalDateTime parsingTime = LocalDateTime.now();
        logger.writeEligibleGamesNumber(eligibleGames);
        //TODO
        for (Day day : Day.values()) {
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX), LocalTime.of(23, 0));
            Map<RuleNumber, List<Game>> dayEligibleGames = processDayEligibleGames(eligibleGames, day);
            GameContainer gameContainer = new GameContainer(
                    parsingTime,
                    dayEligibleGames);
            repositoryFactory.getRepository(day).processGameSaving(gameContainer, startBetTime);
        }
        return new GameContainer(parsingTime, eligibleGames);
    }*/

/*
    private Map<RuleNumber, List<Game>> processDayEligibleGames(Map<RuleNumber, List<Game>> eligibleGames, Day day) {
        Map<RuleNumber, List<Game>> dayEligibleGames = new HashMap<>();
        for (RuleNumber ruleNumber : eligibleGames.keySet()) {
            dayEligibleGames.put(ruleNumber,
                    eligibleGames.get(ruleNumber).stream()
                            .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                            .collect(Collectors.toList()));
        }
        return dayEligibleGames;
    }
*/


/*    private Map<RuleNumber, List<Game>> splitGamesByRules(List<Game> games) throws IOException {
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(RuleNumber.RULE_ONE, new FirstRule().filter(games));
        return eligibleGames;
    }*/
}
