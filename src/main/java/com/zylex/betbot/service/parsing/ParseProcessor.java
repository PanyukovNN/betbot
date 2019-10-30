package com.zylex.betbot.service.parsing;

import com.zylex.betbot.OneXBetBot;
import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.rule.Rule;

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
     * Get links on leagues which include football matches for a specified day,
     * then pull information about matches from every link, put them into list,
     * filter the list by specified Rule, and save all games and filtered games
     * in separate files.
     * @param driverManager - manager for web drivers.
     * @param rule - rule for filter matches.
     * @return - game container returned after filtering.
     */
    public EligibleGameContainer process(DriverManager driverManager, Rule rule, boolean fromFile) {
        ExecutorService service = Executors.newFixedThreadPool(driverManager.getThreads());
        try {
            if (fromFile) {
                List<Game> games = Repository.readGamesFromFile("all_matches_");
                return rule.filter(games);
            }
            ConsoleLogger.startLogMessage(LogType.LEAGUES, null);
            LeagueParser leagueParser = new LeagueParser(driverManager);
            List<String> leagueLinks = leagueParser.processLeagueParsing(OneXBetBot.day);
            ConsoleLogger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(service, driverManager, leagueLinks, OneXBetBot.day);
            ConsoleLogger.addTotalGames(games.size());
            return rule.filter(games);
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        } finally {
            service.shutdown();
            driverManager.quitDrivers();
            ConsoleLogger.parsingSummarizing();
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
