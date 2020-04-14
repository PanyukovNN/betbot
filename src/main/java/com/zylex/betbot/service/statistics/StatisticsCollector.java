package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.StatisticsConsoleLogger;
import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.game.GameResult;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.repository.GameRepository;
import com.zylex.betbot.service.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
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
        logger.startLogMessage(startDate, endDate);
        List<Rule> rules = new ArrayList<>();
        rules.add(ruleRepository.getByName("X_TWO"));
        rules.add(ruleRepository.getByName("FW_SW"));
        for (Rule rule : rules) {
            List<Game> ruleGames = findRuleGames(rule);
            Map<GameResult, List<Game>> resultGames = findResultGames(ruleGames);
            Map<BetCoefficient, Double> betProfit = findBetProfit(ruleGames.size(), resultGames);
            logger.writeRuleStatistics(rule, resultGames, betProfit);
        }
    }

    private Map<GameResult, List<Game>> findResultGames(List<Game> ruleGames) {
        Map<GameResult, List<Game>> resultGames = new HashMap<>();
        for (GameResult gameResult : GameResult.values()) {
            resultGames.put(gameResult, ruleGames.stream()
                    .filter(game -> game.getResult().equals(gameResult.toString()))
                    .collect(Collectors.toList()));
        }
        return resultGames;
    }

    private List<Game> findRuleGames(Rule rule) {
        return gameRepository.getAll()
                        .stream()
                        .filter(game -> !game.getResult().equals(GameResult.NO_RESULT.toString())
                                && !game.getResult().equals(GameResult.NOT_FOUND.toString()))
                        .filter(game -> game.getRules().contains(rule))
                        .collect(Collectors.toList());
    }

    private Map<BetCoefficient, Double> findBetProfit(int totalGamesNumber, Map<GameResult, List<Game>> resultGames) {
        Map<BetCoefficient, Double> betProfit = new LinkedHashMap<>();
        betProfit.put(BetCoefficient.FIRST_WIN, resultGames.get(GameResult.FIRST_WIN).stream().mapToDouble(game -> game.getGameInfo().getFirstWin()).sum() - totalGamesNumber);
        betProfit.put(BetCoefficient.TIE, resultGames.get(GameResult.TIE).stream().mapToDouble(game -> game.getGameInfo().getTie()).sum() - totalGamesNumber);
        betProfit.put(BetCoefficient.SECOND_WIN, resultGames.get(GameResult.SECOND_WIN).stream().mapToDouble(game -> game.getGameInfo().getSecondWin()).sum() - totalGamesNumber);
        betProfit.put(BetCoefficient.ONE_X, resultGames.get(GameResult.FIRST_WIN).stream().mapToDouble(game -> game.getGameInfo().getOneX()).sum()
                + resultGames.get(GameResult.TIE).stream().mapToDouble(game -> game.getGameInfo().getOneX()).sum()
                - totalGamesNumber);
        betProfit.put(BetCoefficient.X_TWO, resultGames.get(GameResult.TIE).stream().mapToDouble(game -> game.getGameInfo().getXTwo()).sum()
                + resultGames.get(GameResult.SECOND_WIN).stream().mapToDouble(game -> game.getGameInfo().getXTwo()).sum()
                - totalGamesNumber);
        return betProfit;
    }
}
