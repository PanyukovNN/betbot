package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.RepositoryFactory;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.RuleProcessorException;
import com.zylex.betbot.model.GameContainer;
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

    private RepositoryFactory repositoryFactory;

    public RuleProcessor(RepositoryFactory repositoryFactory, ParseProcessor parseProcessor, boolean refresh) {
        this.repositoryFactory = repositoryFactory;
        this.parseProcessor = parseProcessor;
        this.refresh = refresh;
    }

    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    /**
     * Filters games by rules, puts in GameContainer and returns it.
     * @return - container of all lists of games.
     */
    public Map<Day, GameContainer> process() {
        try {
            List<Game> games = processDayGames();
            Map<RuleNumber, List<Game>> eligibleGames = splitGamesByRules(games);
            eligibleGames.values().forEach(gameList -> gameList.sort(Comparator.comparing(Game::getDateTime)));
            return processGameContainerMap(eligibleGames);
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private Map<Day, GameContainer> processGameContainerMap(Map<RuleNumber, List<Game>> eligibleGames) {
        LocalDateTime parsingTime = LocalDateTime.now();
        logger.writeEligibleGamesNumber(eligibleGames);
        Map<Day, GameContainer> gameContainerMap = new HashMap<>();
        for (Day day : Day.values()) {
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX), LocalTime.of(23, 0));
            Map<RuleNumber, List<Game>> dayEligibleGames = processDayEligibleGames(eligibleGames, day);
            GameContainer gameContainer = new GameContainer(
                    parsingTime,
                    dayEligibleGames);
            repositoryFactory.getRepository(day).processGameSaving(gameContainer, startBetTime);
            gameContainerMap.put(day, gameContainer);
        }
        return gameContainerMap;
    }

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

    private List<Game> processDayGames() {
        List<Game> games = parseProcessor.process();
        List<Game> betGames = new ArrayList<>();
        for (Day day : Day.values()) {
            LocalDateTime parsingTime = repositoryFactory.getRepository(day).readInfoFile();
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
    }

    private Map<RuleNumber, List<Game>> splitGamesByRules(List<Game> games) throws IOException {
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(RuleNumber.RULE_ONE, new FirstRule().filter(games));
        return eligibleGames;
    }
}
