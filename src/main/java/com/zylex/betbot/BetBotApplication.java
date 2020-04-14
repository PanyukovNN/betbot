package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.rule.RuleProcessor;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsCollector;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@ComponentScan
public class BetBotApplication {

    public static LocalDateTime botStartTime = LocalDateTime.now();

    public static LocalTime betStartTime = LocalTime.of(22, 0);

    public static void main(String[] args) {
        ConsoleLogger.startMessage();
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BetBotApplication.class)) {
//            context.getBean(RuleProcessor.class).process();
//            context.getBean(BetProcessor.class).process();
//            context.getBean(ResultScanner.class).scan(LocalDate.now().minusDays(3));
            context.getBean(StatisticsCollector.class).analyse(null, null);
        } finally {
            ConsoleLogger.endMessage();
        }
    }
}
