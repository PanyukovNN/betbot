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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Map<Day, List<Game>> dayGames = processDayGames();
            Map<Day, Map<RuleNumber, List<Game>>> eligibleGamesMap = splitGamesByRules(dayGames);
            for (Day day : eligibleGamesMap.keySet()) {
                eligibleGamesMap.get(day).forEach((ruleNumber, gameList) -> gameList.sort(Comparator.comparing(Game::getDateTime)));
            }
            return processGameContainerMap(eligibleGamesMap);
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private Map<Day, GameContainer> processGameContainerMap(Map<Day, Map<RuleNumber, List<Game>>> eligibleGamesMap) {
        LocalDateTime parsingTime = LocalDateTime.now();
        logger.writeEligibleGamesNumber(eligibleGamesMap);
        Map<Day, GameContainer> gameContainerMap = new HashMap<>();
        for (Day day : Day.values()) {
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX), LocalTime.of(23, 0));
            GameContainer gameContainer = new GameContainer(parsingTime, eligibleGamesMap.get(day));
            repositoryFactory.getRepository(day).processGameSaving(gameContainer, startBetTime);
            gameContainerMap.put(day, gameContainer);
        }
        return gameContainerMap;
    }

    private Map<Day, List<Game>> processDayGames() {
        List<Game> games = parseProcessor.process();
        Map<Day, List<Game>> dayGames = new HashMap<>();
        for (Day day : Day.values()) {
            //TODO fix ruleNumber
            dayGames.put(day, repositoryFactory.getRepository(day).readRuleFile(RuleNumber.RULE_ONE));
            LocalDateTime parsingTime = repositoryFactory.getRepository(day).readInfoFile();
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1).plusDays(day.INDEX), LocalTime.of(23, 0));
            if (parsingTime.isBefore(startBetTime) || refresh) {
                dayGames.put(day, games.stream()
                        .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                        .collect(Collectors.toList()));
                System.out.print("\n" + Day.TODAY + " games updated.");
            }
        }
        return dayGames;
    }

    private Map<Day, Map<RuleNumber, List<Game>>> splitGamesByRules(Map<Day, List<Game>> dayGames) throws IOException {
        Map<Day, Map<RuleNumber, List<Game>>> eligibleGamesMap = new HashMap<>();
        for (Day day : dayGames.keySet()) {
            Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
            eligibleGames.put(RuleNumber.RULE_ONE, new FirstRule().filter(dayGames.get(day)));
            eligibleGamesMap.put(day, eligibleGames);
        }
        return eligibleGamesMap;
    }
}
