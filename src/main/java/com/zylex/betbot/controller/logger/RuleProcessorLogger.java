package com.zylex.betbot.controller.logger;

import com.zylex.betbot.model.bet.BetStatus;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.Day;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Log RuleProcessor.
 */
public class RuleProcessorLogger extends ConsoleLogger{

    private final static Logger LOG = LoggerFactory.getLogger(RuleProcessor.class);

    /**
     * Log number of eligible games for every rule.
     * @param eligibleGames - list of eligible games.
     * @param activatedRules - list of activated rules.
     */
    public void writeEligibleGamesNumber(List<Game> eligibleGames, List<Rule> activatedRules) {
        if (eligibleGames.isEmpty()) {
            writeInLine("\n" + "No eligible games for activated ruled.");
            LOG.info("No eligible games for activated ruled.");
            writeLineSeparator('~');
            return;
        }
        for (Rule rule : activatedRules) {
            StringBuilder output = new StringBuilder(String.format("%5s:", rule));
            List<Game> ruleGames = findRuleGames(eligibleGames, rule);
            for (Day day : Day.values()) {
                List<Game> dayRuleGames = ruleGames.stream()
                        .filter(game -> game.getDateTime().toLocalDate().isEqual(LocalDate.now().plusDays(day.INDEX)))
                        .collect(Collectors.toList());
                int noBetGamesCount = (int) dayRuleGames.stream()
                        .filter(game -> game.getDateTime().isAfter(LocalDateTime.now()))
                        .filter(game -> game.getBets().isEmpty())
                        .count();
                output.append(String.format("%8s", dayRuleGames.size() + "(+" + noBetGamesCount + ")"));
            }
            writeInLine("\n" + output.toString());
            LOG.info(output.toString());
        }
        writeLineSeparator('~');
    }

    private List<Game> findRuleGames(List<Game> eligibleGames, Rule rule) {
        return eligibleGames.stream()
                .filter(game -> game.getRules().contains(rule))
                .filter(game -> game.getBets().isEmpty() ||
                        game.getBets().stream().anyMatch(bet -> bet.getRule().equals(rule)
                                && bet.getStatus().equals(BetStatus.SUCCESS.toString())))
                .collect(Collectors.toList());
    }
}
