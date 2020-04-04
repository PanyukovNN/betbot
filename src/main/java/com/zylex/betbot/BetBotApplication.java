package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
import com.zylex.betbot.service.rule.RuleNumber;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalTime;
import java.util.*;

@ComponentScan
public class BetBotApplication {

    public static LocalTime betStartTime = LocalTime.of(22, 0);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BetBotApplication.class);
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
        List<RuleNumber> ruleNumberList = defineRuleNumbers(args);
        if (!ruleNumberList.isEmpty()) {
            List<Game> games = context.getBean(ParseProcessor.class).process();
            Map<RuleNumber, List<Game>> ruleGames = context.getBean(RuleProcessor.class).process(games);
            context.getBean(BetProcessor.class).process(ruleGames, ruleNumberList);
        }
        ConsoleLogger.writeToLogFile();
    }

    private static List<RuleNumber> defineRuleNumbers(String[] args) {
        List<RuleNumber> ruleNumberList = new ArrayList<>();
        ruleNumberList.add(RuleNumber.X_TWO);
        return ruleNumberList;
//        if (args.length > 0) {
//            if (Arrays.asList(args).contains("-1")) {
//                ruleNumbers.add(RuleNumber.RULE_ONE);
//            }
//            if (Arrays.asList(args).contains("-2")) {
//                ruleNumbers.add(RuleNumber.RULE_TEST);
//            }
//        }
    }
}
