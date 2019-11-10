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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OneXBetBot {

    public static void main(String[] args) {
        Day day = Day.TODAY;
        RuleNumber ruleNumber = RuleNumber.RULE_ONE;
        boolean mock = true;//args[0].equals("true");
        boolean doBets = false;//args[1].equals("true");
        boolean leaguesFromFile = false;
        boolean refresh = true;

        try {
            new BetProcessor(
                new RuleProcessor(
                    new ParsingRepository(),
                    new ParseProcessor(
                        leaguesFromFile),
                    refresh,
                    day),
                new BetRepository(day, ruleNumber),
                ruleNumber,
                mock,
                doBets
            ).process();

            new ResultScanner(
                new BetRepository(day, ruleNumber)
            ).process();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }
}
