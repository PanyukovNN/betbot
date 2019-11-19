package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.LogType;
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

    private Map<Day, Repository> dayRepository;

    public RuleProcessor(Map<Day, Repository> dayRepository, ParseProcessor parseProcessor, boolean refresh) {
        this.dayRepository = dayRepository;
        this.parseProcessor = parseProcessor;
        this.refresh = refresh;
    }

    public Map<Day, Repository> getDayRepository() {
        return dayRepository;
    }

    /**
     * Filters games by rules, puts in GameContainer and returns it.
     * @return - container of all lists of games.
     */
    public Map<Day, GameContainer> process() {
        try {
            LocalDateTime startTodayBetTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 0));
            LocalDateTime startTomorrowBetTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));

            Map<Day, List<Game>> dayGames = processDayGames(startTomorrowBetTime, startTodayBetTime);

            Map<Day, Map<RuleNumber, List<Game>>> eligibleGamesMap = splitGamesByRules(dayGames);
            for (Day day : eligibleGamesMap.keySet()) {
                eligibleGamesMap.get(day).forEach((ruleNumber, gameList) -> gameList.sort(Comparator.comparing(Game::getDateTime)));
            }

            logger.writeEligibleGamesNumber(eligibleGamesMap);
            GameContainer todayGameContainer = new GameContainer(dayGames.get(Day.TODAY),
                    eligibleGamesMap.get(Day.TODAY));
            dayRepository.get(Day.TODAY).processGameSaving(todayGameContainer, startTodayBetTime);

            GameContainer tomorrowGameContainer = new GameContainer(dayGames.get(Day.TOMORROW),
                    eligibleGamesMap.get(Day.TOMORROW));
            dayRepository.get(Day.TOMORROW).processGameSaving(tomorrowGameContainer, startTomorrowBetTime);

            Map<Day, GameContainer> gameContainerMap = new HashMap<>();
            gameContainerMap.put(Day.TODAY, todayGameContainer);
            gameContainerMap.put(Day.TOMORROW, tomorrowGameContainer);

            return gameContainerMap;
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private Map<Day, List<Game>> processDayGames(LocalDateTime startTomorrowBetTime, LocalDateTime startTodayBetTime) {
        Map<Day, List<Game>> dayGames = new HashMap<>();
        for (Day day : Day.values()) {
            dayGames.put(day, dayRepository.get(day).readAllMatchesFile());
        }

        boolean refreshTodayGames = dayGames.get(Day.TODAY).stream().anyMatch(game -> game.getParsingTime().isBefore(startTodayBetTime));
        boolean refreshTomorrowGames = dayGames.get(Day.TOMORROW).stream().anyMatch(game -> game.getParsingTime().isBefore(startTomorrowBetTime));

        if (refreshTodayGames
                || refreshTomorrowGames
                || dayGames.get(Day.TODAY).isEmpty()
                || dayGames.get(Day.TOMORROW).isEmpty()
                || refresh) {
            List<Game> games = parseProcessor.process();
            if (refreshTodayGames || dayGames.get(Day.TODAY).isEmpty() || refresh) {
                dayGames.put(Day.TODAY, games.stream().filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(Day.TODAY.INDEX))).collect(Collectors.toList()));
                System.out.print("\nПовторно отсканированы сегодняшние игры");
            }
            if (refreshTomorrowGames || dayGames.get(Day.TOMORROW).isEmpty() || refresh) {
                dayGames.put(Day.TOMORROW, games.stream().filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(Day.TOMORROW.INDEX))).collect(Collectors.toList()));
                System.out.print("\nПовторно отсканированы завтрашние игры");
            }
        } else {
            logger.startLogMessage(LogType.PARSING_FILE_START, 0);
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
