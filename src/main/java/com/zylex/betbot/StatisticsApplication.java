package com.zylex.betbot;

import com.zylex.betbot.controller.repository.GameRepository;
import com.zylex.betbot.controller.repository.LeagueRepository;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;

import java.time.LocalDate;

public class StatisticsApplication {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2019, 12, 2);
        LocalDate endDate = LocalDate.now().minusDays(1);

        new StatisticsAnalyser(
            new ResultScanner(
                new GameRepository()
            ),
            new LeagueRepository()
        ).analyse(startDate, endDate);
    }
}
