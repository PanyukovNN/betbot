package com.zylex.betbot.service;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
import com.zylex.betbot.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParseProcessor {

    public List<Game> process(DriverManager driverManager) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(driverManager.getThreads());
        try {
            ConsoleLogger.startLogMessage(LogType.LEAGUES, null);
            LeagueParser leagueParser = new LeagueParser(driverManager);
            List<String> leagueLinks = leagueParser.processLeagueParsing();
            ConsoleLogger.startLogMessage(LogType.GAMES, leagueLinks.size());
            return processGameParsing(service, driverManager, leagueLinks);
        } finally {
            service.shutdown();
        }
    }

    private List<Game> processGameParsing(ExecutorService service,
                                          DriverManager driverManager,
                                          List<String> leagueLinks) throws InterruptedException, ExecutionException {
        List<CallableGameParser> callableGameParsers = new ArrayList<>();
        for (String leagueLink : leagueLinks) {
            callableGameParsers.add(new CallableGameParser(driverManager, leagueLink));
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
        ConsoleLogger.totalGames.addAndGet(games.size());
        return games;
    }
}
