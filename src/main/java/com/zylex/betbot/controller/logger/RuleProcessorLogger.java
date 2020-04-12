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
     */
    public void writeEligibleGamesNumber(List<Game> eligibleGames) {
        Set<Rule> ruleSet = new LinkedHashSet<>();
        eligibleGames.forEach(game -> ruleSet.addAll(game.getRules()));
        List<Rule> ruleList = ruleSet.stream()
                .sorted(Comparator.comparing(Rule::getId))
                .collect(Collectors.toList());
        for (Rule rule : ruleList) {
            StringBuilder output = new StringBuilder(String.format("%11s:", rule));
            List<Game> ruleGames = eligibleGames.stream()
                    .filter(game -> game.getRules().contains(rule))
                    .filter(game -> game.getBets().isEmpty() ||
                            game.getBets().stream().anyMatch(bet -> bet.getRule().equals(rule)
                            && bet.getStatus().equals(BetStatus.SUCCESS.toString())))
                    .collect(Collectors.toList());
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
        writeLineSeparator();
    }
}
