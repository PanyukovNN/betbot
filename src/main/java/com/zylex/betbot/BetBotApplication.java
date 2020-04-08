package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.service.statistics.ResultScanner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.time.LocalTime;

@ComponentScan
public class BetBotApplication {

    public static LocalDateTime botStartTime = LocalDateTime.now();

    public static LocalTime betStartTime = LocalTime.of(22, 0);

    public static void main(String[] args) {
        ConsoleLogger.startMessage();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BetBotApplication.class);
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
        context.getBean(ResultScanner.class).scan();
//        if (args.length > 0) {
//            List<Game> games = context.getBean(ParseProcessor.class).process();
//            Map<Rule, List<Game>> ruleGames = context.getBean(RuleProcessor.class).process(games);
//            context.getBean(BetProcessor.class).process(ruleGames, Arrays.asList(args));
//        }
        ConsoleLogger.endMessage();
    }
}
