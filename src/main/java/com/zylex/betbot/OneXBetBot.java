package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.statistics.ResultScanner;

public class OneXBetBot {

    public static void main(String[] args) {
        try {
            Day day = Day.TOMORROW;
            boolean mock = true;//args[0].equals("true");
            boolean doBets = true;//args[1].equals("true");
            new BetProcessor(
                    new Repository(
                            new RuleProcessor(
                                    new ParseProcessor(day)),
                            day),
                    RuleNumber.RULE_ONE
            ).process(mock, doBets);

            new ResultScanner().process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
