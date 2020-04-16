package com.zylex.betbot.controller.rest;

import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("statistics")
public class StatisticsRestController {

    private ResultScanner resultScanner;

    private StatisticsAnalyser statisticsAnalyser;

    @Autowired
    public StatisticsRestController(ResultScanner resultScanner,
                                    StatisticsAnalyser statisticsAnalyser) {
        this.resultScanner = resultScanner;
        this.statisticsAnalyser = statisticsAnalyser;
    }

    @GetMapping("/scan")
    public ResponseEntity<String> getAll() {
        try {
            resultScanner.scan(LocalDate.now().minusDays(3));
            return new ResponseEntity<>("Scanning successful!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/analyse")
    public ResponseEntity<Map<Rule, Map<BetCoefficient, Double>>> getAnalysedStatistics() {
        try {
            Map<Rule, Map<BetCoefficient, Double>> ruleBetProfit = statisticsAnalyser.analyse(null, null);
            return new ResponseEntity<>(ruleBetProfit, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
