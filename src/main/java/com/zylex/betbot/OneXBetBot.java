package com.zylex.betbot;

import com.zylex.betbot.controller.RepositoryFactory;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static void main(String[] args) {
        RuleNumber ruleNumber = RuleNumber.RULE_ONE;
        boolean mock = true;
        boolean leaguesFromFile = false;
        boolean refresh = false;//args.length > 0 && args[0].equals("true");

        try {
            new BetProcessor(
                new RuleProcessor(
                    new RepositoryFactory(ruleNumber),
                    new ParseProcessor(leaguesFromFile),
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
