package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parsing football games for a next day from 1xStavka.ru
 */
public class ParseProcessor {

    private ParsingConsoleLogger logger = new ParsingConsoleLogger();

    private ExecutorService service;

    private DriverManager driverManager;

    private Day day;

    public ParseProcessor(DriverManager driverManager, Day day) {
        this.driverManager = driverManager;
        this.day = day;
    }

    /**
     * Get links on leagues which include football matches for a specified day,
     * then pull information about matches from every link, put them into list,
     * filter the list by specified Rule, and save all games and filtered games
     * in separate files.
     * @return - list of games.
     */
    public List<Game> process() {
        try {
            driverManager.initiateDrivers(true);
            service = Executors.newFixedThreadPool(driverManager.getThreads());
            logger.startLogMessage(LogType.LEAGUES, null);
            LeagueParser leagueParser = new LeagueParser(logger, driverManager);
            List<String> leagueLinks = leagueParser.processLeagueParsing(day);
            logger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(service, driverManager, leagueLinks, day);
            logger.addTotalGames(games.size());
            return games;
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        } finally {
            service.shutdown();
            driverManager.quitDrivers();
            logger.parsingSummarizing();
        }
    }

    private List<Game> processGameParsing(ExecutorService service,
                                          DriverManager driverManager,
                                          List<String> leagueLinks,
                                          Day day) throws InterruptedException, ExecutionException {
        List<CallableGameParser> callableGameParsers = new ArrayList<>();
        for (String leagueLink : leagueLinks) {
            callableGameParsers.add(new CallableGameParser(logger, driverManager, leagueLink, day));
        }
        List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);
        List<Game> games = new ArrayList<>();
        for (Future<List<Game>> gameList : futureGameParsers) {
            games.addAll(gameList.get());
        }
        return games;
    }
}
