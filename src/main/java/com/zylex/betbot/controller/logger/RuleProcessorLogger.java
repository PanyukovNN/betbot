package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.rule.RuleNumber;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Logs BetProcessor.
 */
public class RuleProcessorLogger extends ConsoleLogger{

    /**
     * Log number of eligible games for every rule.
     * @param eligibleGames - map of eligible games.
     */
    public void writeEligibleGamesNumber(Map<RuleNumber, List<Game>> eligibleGames) {
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            writeInLine(String.format("\n%13s ", ruleNumber + " -"));
            for (Day day : Day.values()) {
                int eligibleGamesCount = (int) eligibleGames.get(ruleNumber).stream()
                        .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                        .count();
                writeInLine(String.format("%3d (%s) ", eligibleGamesCount, day));
            }
        }
        writeLineSeparator();
    }
}
