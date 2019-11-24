package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parsing football games from the site.
 */
public class ParseProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ExecutorService service = Executors.newFixedThreadPool(8);

    /**
     * Gets links on leagues which include football matches, then pulls information about matches from every link,
     * puts matches into list, and return it.
     * @return - list of games.
     */
    public List<Game> process() {
        try {
            logger.startLogMessage(LogType.PARSING_SITE_START, 0);
            List<String> leagueLinks = new LeagueParser(logger)
                    .processLeagueParsing();
            logger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(service, leagueLinks);
            logger.addTotalTodayGames((int) games.stream().filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(Day.TODAY.INDEX))).count());
            logger.addTotalTomorrowGames((int) games.stream().filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(Day.TOMORROW.INDEX))).count());
            return games;
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        } finally {
            service.shutdown();
            logger.parsingSummarizing();
        }
    }

    private List<Game> processGameParsing(ExecutorService service,
                                          List<String> leagueLinks) throws InterruptedException, ExecutionException {
        List<CallableGameParser> callableGameParsers = new ArrayList<>();
        for (String leagueLink : leagueLinks) {
            callableGameParsers.add(new CallableGameParser(logger, leagueLink));
        }
        List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);
        List<Game> games = new ArrayList<>();
        for (Future<List<Game>> gameList : futureGameParsers) {
            games.addAll(gameList.get());
        }
        return games;
    }
}
