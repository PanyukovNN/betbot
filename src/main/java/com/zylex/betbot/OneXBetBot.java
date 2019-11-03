package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.time.LocalDate;

public class OneXBetBot {

    public static void main(String[] args) {
        int threads = 6;
//        BetCoefficient.FIRST_WIN.PERCENT = 0.05d;
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
        ).process(true, false);
    }
}
