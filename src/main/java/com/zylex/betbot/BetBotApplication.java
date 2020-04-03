package com.zylex.betbot;

import com.zylex.betbot.controller.logger.ConsoleLogger;

import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.rule.RuleNumber;
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
            context.getBean(BetProcessor.class).process(ruleNumberList);
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
