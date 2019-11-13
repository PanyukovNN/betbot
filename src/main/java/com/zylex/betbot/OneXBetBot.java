package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static void main(String[] args) {
        Day day = Day.TOMORROW;
        RuleNumber ruleNumber = RuleNumber.RULE_ONE;
        boolean mock = true;
        boolean doBets = false;
        boolean leaguesFromFile = false;
        boolean refresh = false;

        try {
            new BetProcessor(
                new RuleProcessor(
                    new Repository(day, ruleNumber),
                    new ParseProcessor(leaguesFromFile),
                    refresh),
                ruleNumber,
                mock,
                doBets
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
