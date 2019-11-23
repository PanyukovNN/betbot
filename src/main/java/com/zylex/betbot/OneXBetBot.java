package com.zylex.betbot;

import com.zylex.betbot.controller.GameRepository;
import com.zylex.betbot.controller.LeagueRepository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static void main(String[] args) {
        RuleNumber ruleNumber = RuleNumber.RULE_TEST;
        boolean mock = true;//args.length > 0 && Arrays.asList(args).contains("-m");
        boolean leaguesFromFile = false;//args.length > 0 && Arrays.asList(args).contains("-f");
        boolean refresh = false;//args.length > 0 && Arrays.asList(args).contains("-r");

        try {
            new BetProcessor(
                new RuleProcessor(
                    new GameRepository(ruleNumber),
                    new LeagueRepository(),
                    new ParseProcessor(leaguesFromFile),
                    refresh
                ),
                mock
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
