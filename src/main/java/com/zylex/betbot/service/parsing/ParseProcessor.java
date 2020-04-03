package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.ParseProcessorException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.repository.GameInfoRepository;
import com.zylex.betbot.service.repository.GameRepository;
import com.zylex.betbot.service.repository.LeagueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Parsing football games from the site.
 */
@Service
public class ParseProcessor {

//    @Autowired
//    private ThreadPoolTaskExecutor threadPool;

    private LeagueLinksParser leagueLinksParser;

//    private LeagueRepository leagueRepository;
//
//    private GameRepository gameRepository;
//
//    private GameInfoRepository gameInfoRepository;

    private CallableGameParser callableGameParser;

    @Autowired
    public ParseProcessor(LeagueLinksParser leagueLinksParser,
                          CallableGameParser callableGameParser) {
        this.leagueLinksParser = leagueLinksParser;
        this.callableGameParser = callableGameParser;
    }

    /**
     * Gets links on leagues which include football matches, then pulls information about matches from every link,
     * puts matches into list, and return it.
     * @return - list of games.
     */
    @Transactional
    public List<Game> process() {
//        ExecutorService service = Executors.newWorkStealingPool();
        try {
            ParsingConsoleLogger.startLogMessage(LogType.PARSING_SITE_START, 0);
            List<String> leagueLinks = leagueLinksParser.processLeagueParsing();
            ParsingConsoleLogger.startLogMessage(LogType.GAMES, leagueLinks.size());
            List<Game> games = processGameParsing(leagueLinks);
            ParsingConsoleLogger.writeTotalGames(games);
            return games;
        } catch (InterruptedException | ExecutionException e) {
            throw new ParseProcessorException(e.getMessage(), e);
        } finally {
//            service.shutdown();
        }
    }

    private List<Game> processGameParsing(List<String> leagueLinks) throws InterruptedException, ExecutionException {
//        List<CallableGameParser> callableGameParsers = new ArrayList<>();
//        List<Future<List<Game>>> futureGameParsers = new ArrayList<>();
//        for (String leagueLink : leagueLinks) {
////            callableGameParsers.add(new CallableGameParser(leagueLink, leagueRepository, ));
//            CallableGameParser gameParser = new CallableGameParser(leagueLink, leagueRepository, gameRepository, gameInfoRepository);
//            Future<List<Game>> future = threadPool.submit(gameParser);
//            futureGameParsers.add(future);
//        }
        List<Game> games = new ArrayList<>();
        for (String leagueLink : leagueLinks) {
            games.addAll(callableGameParser.process(leagueLink));
        }
//        for (Future<List<Game>> gameList : futureGameParsers) {
//            games.addAll(gameList.get());
//        }
        return games;
    }
}
