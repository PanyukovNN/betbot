package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;

import java.time.LocalDate;

public class StatisticsApplication {

    public static void main(String[] args) {
        RuleNumber ruleNumber = RuleNumber.RULE_TEST;

        LocalDate startDate = LocalDate.of(2019, 11, 11);
        LocalDate endDate = LocalDate.now().minusDays(1);

        new StatisticsAnalyser(
            new ResultScanner(
                new Repository(
                    ruleNumber
                )
            )
        ).analyse(startDate, endDate);
    }
}
