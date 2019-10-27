package com.zylex.betbot;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.Saver;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.SecretRule;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.List;

public class OneXBetParserApplication {

    public static void main(String[] args) {
        int threads = 10;//Integer.parseInt(args[0]);
        ParseProcessor parseProcessor = new ParseProcessor();
        List<Game> games = parseProcessor.process(threads);
        Saver saver = new Saver();
        saver.processSaving(games, "results");
        SecretRule rule = new SecretRule();
        List<Game> filteredGames = rule.baseRuleFilter(games);
        System.out.println("\nEligible games: " + ConsoleLogger.eligibleGames);
        saver.processSaving(filteredGames, "filtered_results");
    }
}
