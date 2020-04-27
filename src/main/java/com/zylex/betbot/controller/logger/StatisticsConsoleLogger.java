package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.game.GameResult;
import com.zylex.betbot.model.rule.Rule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Log StatisticsAnalyser.
 */
public class StatisticsConsoleLogger extends ConsoleLogger{

    /**
     * Log start message.
     * @param startDate - start date.
     * @param endDate - end date.
     */
    public synchronized void startLogMessage(LocalDate startDate, LocalDate endDate) {
        writeInLine(String.format("\nAnalyse statistics for period from %s to %s", startDate, endDate));
        writeLineSeparator();
    }

    /**
     * Log formatted statistics.
     * @param rule - specified rule.
     * @param resultGames - map of games by game results.
     * @param betProfit - map of bet profit by coefficients.
     */
    public void writeRuleStatistics(Rule rule, Map<GameResult, List<Game>> resultGames, Map<BetCoefficient, Double> betProfit) {
        writeInLine(String.format("\n%5s: %3s|%3s|%3s %6s|%6s|%6s %6s|%6s",
                rule,
                resultGames.get(GameResult.FIRST_WIN).size(),
                resultGames.get(GameResult.TIE).size(),
                resultGames.get(GameResult.SECOND_WIN).size(),
                String.format("%.2f", betProfit.get(BetCoefficient.FIRST_WIN)),
                String.format("%.2f", betProfit.get(BetCoefficient.TIE)),
                String.format("%.2f", betProfit.get(BetCoefficient.SECOND_WIN)),
                String.format("%.2f", betProfit.get(BetCoefficient.ONE_X)),
                String.format("%.2f", betProfit.get(BetCoefficient.X_TWO))));
    }
}
