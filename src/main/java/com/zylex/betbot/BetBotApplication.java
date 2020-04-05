package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.Rule;
import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalTime;
import java.util.*;

@ComponentScan
public class BetBotApplication {

    public static LocalTime betStartTime = LocalTime.of(22, 0);

    public static void main(String[] args) {
        ConsoleLogger.startMessage();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BetBotApplication.class);
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
        if (args.length > 0) {
            List<Game> games = context.getBean(ParseProcessor.class).process();
            Map<Rule, List<Game>> ruleGames = context.getBean(RuleProcessor.class).process(games);
            context.getBean(BetProcessor.class).process(ruleGames, Arrays.asList(args));
        }
        ConsoleLogger.endMessage();
    }
}
