package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.controller.ParsingRepository;
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

    private ParsingRepository parsingRepository;

    private Day day;

    public RuleProcessor(ParsingRepository parsingRepository, ParseProcessor parseProcessor, boolean refresh, Day day) {
        this.parsingRepository = parsingRepository;
        this.parseProcessor = parseProcessor;
        this.refresh = refresh;
        this.day = day;
    }

    /**
     * Filters games by all rules and puts filtered lists in GameContainer.
     *
     * @return - container of all lists of games.
     */
    public GameContainer process() {
        try {
            //TODO log
            LocalDateTime startBetTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(23, 0));
            List<Game> games = parsingRepository.readGamesFromFile(day);
            if (games.stream().anyMatch(game -> game.getParsingTime().isBefore(startBetTime))
                    || refresh) {
                games = parseProcessor.process(day);
            }
            Map<RuleNumber, List<Game>> eligibleGames = new HashMap<>();
            eligibleGames.put(RuleNumber.RULE_ONE, new FirstWinSecretRule().filter(games));
            eligibleGames.put(RuleNumber.RULE_TWO, new OneXSecretRule().filter(games));
            sortGamesByDate(eligibleGames);
            logger.writeEligibleGamesNumber(eligibleGames);
            GameContainer gameContainer = new GameContainer(games, eligibleGames);
            parsingRepository.saveGamesToFiles(day, gameContainer);
            return gameContainer;
        } catch (IOException e) {
            throw new RuleProcessorException(e.getMessage(), e);
        }
    }

    private void sortGamesByDate(Map<RuleNumber, List<Game>> eligibleGames) {
        for (List<Game> games : eligibleGames.values()) {
            games.sort(Comparator.comparing(Game::getDateTime));
        }
    }
}
