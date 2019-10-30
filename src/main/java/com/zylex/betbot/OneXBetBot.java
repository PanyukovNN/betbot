package com.zylex.betbot;

import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.*;
import com.zylex.betbot.service.bet.rule.RuleSaver;
import com.zylex.betbot.service.bet.rule.firstWinSecretRule;
import com.zylex.betbot.service.bet.rule.oneXSecretRule;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static final Day day = Day.TODAY;

    public static void main(String[] args) {
        int threads = 6;//Integer.parseInt(args[0]);
        new BetProcessor(
                new ParseProcessor().process(
                        new DriverManager(threads, true),
                        new RuleSaver(
                                new firstWinSecretRule()),
                        false),
                true);
    }
}
