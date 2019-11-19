package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.HashMap;
import java.util.Map;

public class OneXBetBot {

    public static void main(String[] args) {
        RuleNumber ruleNumber = RuleNumber.RULE_ONE;
        boolean mock = true;
        boolean doBets = true;
        boolean leaguesFromFile = false;
        boolean refresh = false;//args.length > 0 && args[0].equals("true");

        Map<Day, Repository> dayRepository = new HashMap<>();
        dayRepository.put(Day.TODAY, new Repository(Day.TODAY, ruleNumber));
        dayRepository.put(Day.TOMORROW, new Repository(Day.TOMORROW, ruleNumber));

        try {
            new BetProcessor(
                new RuleProcessor(
                    dayRepository,
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
