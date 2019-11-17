package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;

import java.time.LocalDate;

public class StatisticsApplication {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2019, 11, 11);
        LocalDate endDate = LocalDate.now().minusDays(1);

        new StatisticsAnalyser(
            new ResultScanner(
                new Repository(
                    Day.TODAY,
                    RuleNumber.RULE_ONE
                )
            )
        ).analyse(startDate, endDate);
    }
}
