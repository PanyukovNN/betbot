package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
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

    /**
     * Get links on leagues which include football matches for a next day,
     * then from pull information about matches from every link, put it into
     * list and return.
     * @param threads - number of threads.
     * @return - list of games.
     */
    public List<Game> process(int threads, Day day) {
        DriverManager driverManager = new DriverManager();
        driverManager.initiateDrivers(threads, true);
        ExecutorService service = Executors.newFixedThreadPool(threads);
        try {
            ConsoleLogger.startLogMessage(LogType.LEAGUES, null);
            LeagueParser leagueParser = new LeagueParser(driverManager);
            List<String> leagueLinks = leagueParser.processLeagueParsing(day);
            ConsoleLogger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(service, driverManager, leagueLinks, day);
            ConsoleLogger.addTotalGames(games.size());
            return games;
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        } finally {
            service.shutdown();
            driverManager.quitDrivers();
            ConsoleLogger.totalSummarizing();
        }
    }

    private List<Game> processGameParsing(ExecutorService service,
                                          DriverManager driverManager,
                                          List<String> leagueLinks,
                                          Day day) throws InterruptedException, ExecutionException {
        List<CallableGameParser> callableGameParsers = new ArrayList<>();
        for (String leagueLink : leagueLinks) {
            callableGameParsers.add(new CallableGameParser(driverManager, leagueLink, day));
        }
        List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);
        return convertFutureGames(futureGameParsers);
    }

    private List<Game> convertFutureGames(List<Future<List<Game>>> futureGameParsers) throws InterruptedException, ExecutionException {
        List<Game> games = new ArrayList<>();
        for (Future<List<Game>> gameList : futureGameParsers) {
            List<Game> leagueGames = gameList.get();
            games.addAll(leagueGames);
        }
        return games;
    }
}
