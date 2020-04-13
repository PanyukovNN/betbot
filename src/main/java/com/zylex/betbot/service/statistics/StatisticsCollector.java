package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.StatisticsConsoleLogger;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.game.GameResult;
import com.zylex.betbot.service.repository.GameRepository;
import com.zylex.betbot.service.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyses game results statistics.
 */
@Service
public class StatisticsCollector {

    private StatisticsConsoleLogger logger = new StatisticsConsoleLogger();

    private GameRepository gameRepository;

    private RuleRepository ruleRepository;

    @Autowired
    public StatisticsCollector(GameRepository gameRepository,
                               RuleRepository ruleRepository) {
        this.gameRepository = gameRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Runs ResultScanner, which finds games results.
     * Gets results of games from database and compute them statistics for specified period.
     * @param startDate - start date of period.
     * @param endDate - end date of period.
     */
    @Transactional
    public void analyse(LocalDate startDate, LocalDate endDate) {
//        logger.startLogMessage(startDate, endDate);
//        List<Rule> rules = ruleRepository.getAll();
//        for (Rule rule : rules) {
//            List<Game> games = gameDao.getByRuleNumber(ruleNumber);
//            List<Game> gamesByDatePeriod = filterByDatePeriod(startDate, endDate, games);
//            List<Game> betMadeGamesByLeagues = splitBetMadeGamesByLeagues(gamesByDatePeriod);
//            computeStatistics(ruleNumber, gamesByDatePeriod, betMadeGamesByLeagues);
//        }
//
//     Game printer
        System.out.println();
        List<Game> games = gameRepository.getAll()
                .stream()
                .filter(game -> !game.getResult().equals(GameResult.NO_RESULT.toString()))
                .filter(game -> game.getRules().stream().anyMatch(rule -> rule.getName().equals("FW_SW")))
                .sorted(Comparator.comparing(Game::getResult,
                        Comparator.comparing(GameResult::valueOf))
                        .thenComparing(game -> game.getGameInfo().getFirstWin())
                )
                .collect(Collectors.toList());
//        games.forEach(System.out::println);

        for (GameResult gameResult : GameResult.values()) {
            if (gameResult == GameResult.NO_RESULT || gameResult == GameResult.NOT_FOUND) continue;
            System.out.println(gameResult + ": " +
                    games.stream()
                    .filter(game -> game.getResult().equals(gameResult.toString()))
                    .count());
        }

//        for (GameResult gameResult : GameResult.values()) {
//            if (gameResult.equals(GameResult.NO_RESULT)
//                || gameResult.equals(GameResult.SECOND_WIN)) continue;
//
//            List<Game> resultGames =  games.stream()
//                    .filter(game -> game.getResult().equals(gameResult.toString()))
//                    .collect(Collectors.toList());
//            if (gameResult.equals(GameResult.TIE)) {
//                resultGames.addAll(
//                        games.stream()
//                                .filter(game -> game.getResult().equals(GameResult.SECOND_WIN.toString()))
//                                .collect(Collectors.toList())
//                );
//            }
//
//            double avgFw = resultGames.stream()
//                    .mapToDouble(game -> game.getGameInfo().getFirstWin())
//                    .average()
//                    .orElse(Double.NaN);
//            double avgTie = resultGames.stream()
//                    .mapToDouble(game -> game.getGameInfo().getTie())
//                    .average()
//                    .orElse(Double.NaN);
//            double avgSw = resultGames.stream()
//                    .mapToDouble(game -> game.getGameInfo().getSecondWin())
//                    .average()
//                    .orElse(Double.NaN);
//            double avgOneX = resultGames.stream()
//                    .mapToDouble(game -> game.getGameInfo().getOneX())
//                    .average()
//                    .orElse(Double.NaN);
//            double avgXTwo = resultGames.stream()
//                    .mapToDouble(game -> game.getGameInfo().getXTwo())
//                    .average()
//                    .orElse(Double.NaN);
//
//            System.out.println(
//                    String.format("%s: %.2f|%.2f|%.2f %.2f|%.2f", gameResult, avgFw, avgTie, avgSw, avgOneX, avgXTwo)
//            );
//        }
    }

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
