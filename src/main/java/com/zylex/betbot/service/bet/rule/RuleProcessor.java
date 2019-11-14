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

/**
 * Filters games by rules.
 */
public class RuleProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ParseProcessor parseProcessor;

    private boolean refresh;

    private Repository repository;

    private Day day;

    public RuleProcessor(Repository repository, ParseProcessor parseProcessor, boolean refresh) {
        this.repository = repository;
        this.parseProcessor = parseProcessor;
        this.refresh = refresh;
        this.day = repository.getDay();
    }

    public Repository getRepository() {
        return repository;
    }

    /**
     * Filters games by rules, puts in GameContainer and returns it.
     * @return - container of all lists of games.
     */
    public GameContainer process() {
        try {
            LocalDateTime startBetTime = LocalDateTime.of(
                    LocalDate.now().plusDays(day.INDEX).minusDays(1),
                    LocalTime.of(23, 0));
            List<Game> games = processGames(startBetTime);
            Map<RuleNumber, List<Game>> eligibleGames = splitGamesByRules(games);
            sortGamesByDate(eligibleGames);
            logger.writeEligibleGamesNumber(eligibleGames);
            GameContainer gameContainer = new GameContainer(games, eligibleGames);
            repository.saveGamesToFiles(gameContainer, startBetTime);
            return gameContainer;
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private List<Game> processGames(LocalDateTime startBetTime) {
        List<Game> games = repository.readAllMatchesFile();
        if (games.stream().anyMatch(game -> game.getParsingTime().isBefore(startBetTime))
                || games.isEmpty()
                || refresh) {
            games = parseProcessor.process(day);
        } else {
            logger.startLogMessage(LogType.PARSING_FILE_START, day == Day.TODAY ? 0 : 1);
        }
        return games;
    }

    private Map<RuleNumber, List<Game>> splitGamesByRules(List<Game> games) throws IOException {
        Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
        eligibleGames.put(RuleNumber.RULE_ONE, new FirstWinSecretRule().filter(games));
        eligibleGames.put(RuleNumber.RULE_TWO, new OneXSecretRule().filter(games));
        return eligibleGames;
    }

    private void sortGamesByDate(Map<RuleNumber, List<Game>> eligibleGames) {
        for (List<Game> games : eligibleGames.values()) {
            games.sort(Comparator.comparing(Game::getDateTime));
        }
    }
}
