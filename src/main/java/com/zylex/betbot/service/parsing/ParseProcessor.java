package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parsing football games from the site.
 */
@Service
public class ParseProcessor {

    private LeagueLinksParser leagueLinksParser;

    @Autowired
    public ParseProcessor(LeagueLinksParser leagueLinksParser) {
        this.leagueLinksParser = leagueLinksParser;
    }

    /**
     * Gets links on leagues which include football matches, then pulls information about matches from every link,
     * puts matches into list, and return it.
     * @return - list of games.
     */
    @Transactional
    public List<Game> process() {
        try {
            ParsingConsoleLogger.startLogMessage(LogType.PARSING_SITE_START, 0);
            List<String> leagueLinks = leagueLinksParser.processLeagueParsing();
            ParsingConsoleLogger.startLogMessage(LogType.LEAGUES, leagueLinks.size());
            List<Game> games = processGameParsing(leagueLinks);
            ParsingConsoleLogger.writeTotalGames(games);
            return games;
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        }
    }

    private List<Game> processGameParsing(List<String> leagueLinks) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newWorkStealingPool();
        try {
            List<CallableGameParser> callableGameParsers = new ArrayList<>();
            for (String leagueLink : leagueLinks) {

                callableGameParsers.add(new CallableGameParser(leagueLink));
            }
            List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);
            List<Game> games = new ArrayList<>();
            for (Future<List<Game>> gameList : futureGameParsers) {
                games.addAll(gameList.get());
            }
            return games;
        } finally {
            service.shutdown();
        }
    }
}
