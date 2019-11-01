package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static void main(String[] args) {
        int threads = 6;
        Day day = Day.TOMORROW;
        new BetProcessor(
            new Repository(
                new RuleProcessor(
                    new ParseProcessor(
                        new DriverManager(threads),
                        day
                    )
                ),
                day
            ).processSaving(),
            RuleNumber.ONE
        ).process(true, true);
    }
}
