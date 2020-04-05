package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.Day;
import com.zylex.betbot.model.Rule;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Log RuleProcessor.
 */
public class RuleProcessorLogger extends ConsoleLogger{

    private final static Logger LOG = LoggerFactory.getLogger(RuleProcessor.class);

    /**
     * Log number of eligible games for every rule.
     * @param eligibleGames - map of eligible games.
     */
    public void writeEligibleGamesNumber(Map<Rule, List<Game>> eligibleGames) {
        for (Rule ruleNumber : eligibleGames.keySet()) {
            StringBuilder output = new StringBuilder(String.format("%13s ", ruleNumber + " -"));
            for (Day day : Day.values()) {
                int eligibleGamesCount = (int) eligibleGames.get(ruleNumber).stream()
                        .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                        .count();
                output.append(String.format("%3d (%s) ", eligibleGamesCount, day));
            }
            writeInLine("\n" + output.toString());
            LOG.info(output.toString());
        }
        writeLineSeparator();
    }
}
