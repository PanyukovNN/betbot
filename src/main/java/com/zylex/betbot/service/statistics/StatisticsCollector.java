package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.StatisticsConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyses game results statistics.
 */
public class StatisticsCollector {

//    private StatisticsConsoleLogger logger = new StatisticsConsoleLogger();
//
//    private LeagueDao leagueDao;
//
//    private GameDao gameDao;
//
//    public StatisticsCollector(LeagueDao leagueDao, GameDao gameDao) {
//        this.leagueDao = leagueDao;
//        this.gameDao = gameDao;
//    }
//
//    /**
//     * Runs ResultScanner, which finds games results.
//     * Gets results of games from database and compute them statistics for specified period.
//     * @param startDate - start date of period.
//     * @param endDate - end date of period.
//     */
//    public void analyse(LocalDate startDate, LocalDate endDate) {
//        logger.startLogMessage(startDate, endDate);
//        for (RuleNumber ruleNumber : RuleNumber.values()) {
//            List<Game> games = gameDao.getByRuleNumber(ruleNumber);
//            List<Game> gamesByDatePeriod = filterByDatePeriod(startDate, endDate, games);
//            List<Game> betMadeGamesByLeagues = splitBetMadeGamesByLeagues(gamesByDatePeriod);
//            computeStatistics(ruleNumber, gamesByDatePeriod, betMadeGamesByLeagues);
//        }
//    }
//
//    private List<Game> splitBetMadeGamesByLeagues(List<Game> betMadeGames) {
//        List<String> selectedLeagues = leagueDao.getAllSelectedLeagues();
//        return betMadeGames.stream()
//                .filter(game -> selectedLeagues.contains(game.getLeagueLink()))
//                .collect(Collectors.toList());
//    }
//
//    private List<Game> filterByDatePeriod(LocalDate startDate, LocalDate endDate, List<Game> betMadeGames) {
//        betMadeGames = betMadeGames.stream()
//                .filter(game -> {
//                    LocalDate gameDate = game.getDateTime().toLocalDate();
//                    return (!gameDate.isBefore(startDate) && !gameDate.isAfter(endDate));
//                }).collect(Collectors.toList());
//        return betMadeGames;
//    }
//
//    private void computeStatistics(RuleNumber ruleNumber, List<Game> games1, List<Game> games2) {
//        int firstWins1 = countResult(games1, GameResult.FIRST_WIN);
//        int ties1 = countResult(games1, GameResult.TIE);
//        int secondWins1 = countResult(games1, GameResult.SECOND_WIN);
//        int noResults1 = countResult(games1, GameResult.NO_RESULT);
//        int firstWins2 = countResult(games2, GameResult.FIRST_WIN);
//        int ties2 = countResult(games2, GameResult.TIE);
//        int secondWins2 = countResult(games2, GameResult.SECOND_WIN);
//        int noResults2 = countResult(games2, GameResult.NO_RESULT);
//        logger.logStatistics(ruleNumber,
//                games1.size(), firstWins1, ties1, secondWins1, noResults1,
//                games2.size(), firstWins2, ties2, secondWins2, noResults2);
//    }
//
//    private int countResult(List<Game> games1, GameResult firstWin) {
//        return (int) games1.stream().filter(game -> game.getGameResult().equals(firstWin)).count();
//    }
}
