package com.zylex.betbot;

import com.zylex.betbot.controller.GameRepository;
import com.zylex.betbot.controller.LeagueRepository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.Arrays;

public class BetBotApplication {

    public static void main(String[] args) {
        RuleNumber ruleNumber = RuleNumber.RULE_TEST;
        boolean mock = args.length > 0 && Arrays.asList(args).contains("-m");
        boolean refresh = args.length > 0 && Arrays.asList(args).contains("-r");

        try {
            new BetProcessor(
                new RuleProcessor(
                    new GameRepository(),
                    new LeagueRepository(),
                    new ParseProcessor(),
                    refresh
                ),
                ruleNumber,
                mock
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
