package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.statistics.ResultScanner;

public class OneXBetBot {

    public static void main(String[] args) {
        Day day = Day.TOMORROW;
        boolean mock = true;
        boolean doBets = false;
        new BetProcessor(
            new Repository(
                new RuleProcessor(
                    new ParseProcessor(day)),
                day),
            RuleNumber.RULE_ONE
        ).process(mock, doBets);
        new ResultScanner().process();
    }
}
