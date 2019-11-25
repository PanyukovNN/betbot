package com.zylex.betbot;

import com.zylex.betbot.controller.GameRepository;
import com.zylex.betbot.controller.LeagueRepository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BetBotApplication {

    public static void main(String[] args) {
        try {
            new BetProcessor(
                new RuleProcessor(
                    new GameRepository(),
                    new LeagueRepository(),
                    new ParseProcessor()
                ),
                defineRuleNumbers(args)
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }

    private static List<RuleNumber> defineRuleNumbers(String[] args) {
        List<RuleNumber> ruleNumbers = new ArrayList<>();
        if (args.length > 0) {
            if (Arrays.asList(args).contains("-1")) {
                ruleNumbers.add(RuleNumber.RULE_ONE);
            }
            if (Arrays.asList(args).contains("-2")) {
                ruleNumbers.add(RuleNumber.RULE_TEST);
            }
        }
        return ruleNumbers;
    }
}
