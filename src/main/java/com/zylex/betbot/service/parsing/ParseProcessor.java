package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parsing football games for a next day from 1xStavka.ru
 */
public class ParseProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ExecutorService service = Executors.newFixedThreadPool(8);

    private boolean leaguesFromFile;

    private LocalDateTime parsingTime = LocalDateTime.now();

    public ParseProcessor(boolean leaguesFromFile) {
        this.leaguesFromFile = leaguesFromFile;
    }

    /**
     * Get links on leagues which include football matches for a specified day,
     * then pull information about matches from every link, put matches into list,
     * and return it.
     * @param day - specified day.
     * @return - list of games.
     */
    public List<Game> process() {
        try {
            logger.startLogMessage(LogType.PARSING_SITE_START, 0);
            List<String> leagueLinks = new LeagueParser(logger, leaguesFromFile)
                    .processLeagueParsing();
            logger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(service, leagueLinks);
            logger.addTotalGames(games.size());
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
            callableGameParsers.add(new CallableGameParser(logger, leagueLink, parsingTime));
        }
        List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);
        List<Game> games = new ArrayList<>();
        for (Future<List<Game>> gameList : futureGameParsers) {
            games.addAll(gameList.get());
        }
        return games;
    }
}
