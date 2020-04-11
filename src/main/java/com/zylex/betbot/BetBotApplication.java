package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.repository.RuleRepository;
import com.zylex.betbot.service.rule.RuleProcessor;
import com.zylex.betbot.service.statistics.ResultScanner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ComponentScan
public class BetBotApplication {

    public static LocalDateTime botStartTime = LocalDateTime.now();

    public static LocalTime betStartTime = LocalTime.of(22, 0);

    public static void main(String[] args) {
        ConsoleLogger.startMessage();
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BetBotApplication.class)) {
            if (args.length > 0) {
                List<Game> games = context.getBean(ParseProcessor.class).process();
                List<Game> ruleGames = context.getBean(RuleProcessor.class).process(games);
                context.getBean(BetProcessor.class).process(ruleGames, defineRules(context.getBean(RuleRepository.class), args));
                context.getBean(ResultScanner.class).scan(LocalDate.now().minusDays(3));
            }
        }
        ConsoleLogger.endMessage();
    }

    private static List<Rule> defineRules(RuleRepository ruleRepository, String[] args) {
        List<String> ruleNames = Arrays.asList(args);
        return ruleRepository.getAll().stream()
                .filter(rule -> ruleNames.stream().anyMatch(ruleName -> ruleName.equals(rule.getName())))
                .collect(Collectors.toList());
    }
}
