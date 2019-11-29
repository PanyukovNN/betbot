package com.zylex.betbot;

import com.zylex.betbot.controller.GameRepository;
import com.zylex.betbot.controller.LeagueRepository;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;

import java.time.LocalDate;

public class StatisticsApplication {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2019, 11, 25);
        LocalDate endDate = LocalDate.now();

        new StatisticsAnalyser(
            new ResultScanner(
                new GameRepository()
            ),
            new LeagueRepository()
        ).analyse(startDate, endDate);
    }
}
