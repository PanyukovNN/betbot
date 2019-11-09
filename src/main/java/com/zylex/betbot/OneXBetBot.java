package com.zylex.betbot;

import com.zylex.betbot.controller.BetRepository;
import com.zylex.betbot.controller.ParsingRepository;
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
            Day day = Day.TODAY;
            boolean mock = true;//args[0].equals("true");
            boolean doBets = false;//args[1].equals("true");
            boolean leaguesFromFile = true;
            new BetProcessor(
                new ParsingRepository(
                    new RuleProcessor(
                        new ParseProcessor(
                                day,
                                leaguesFromFile)),
                    day),
                new BetRepository(day),
                RuleNumber.RULE_ONE,
                mock,
                doBets
            ).process();

            new ResultScanner(
                new BetRepository(day)
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
