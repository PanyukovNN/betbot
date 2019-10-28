package com.zylex.betbot;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.bet.Rule;
import com.zylex.betbot.service.bet.SecretRule;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.util.List;

public class OneXBetBot {

    public static void main(String[] args) {
        int threads = Integer.parseInt(args[0]);
        Day day = Day.TOMORROW;
        ParseProcessor parseProcessor = new ParseProcessor();
        Repository repository = new Repository(day);
        Rule rule = new SecretRule();
        List<Game> games = parseProcessor.process(threads, day);
//        List<Game> games = repository.readGamesFromFile("all_matches_");
        repository.processSaving(games, "all_matches_");
        EligibleGameContainer gameContainer = rule.filter(games);
        ConsoleLogger.writeInLine("\nEligible games: " + ConsoleLogger.eligibleGames);
        repository.processSaving(gameContainer.getEligibleGames(), "eligible_matches_");

        ConsoleLogger.writeLineSeparator();
        ConsoleLogger.writeInLine("\n");
        List<Game> mockEligibleGames = repository.readGamesFromFile("eligible_matches_");
        EligibleGameContainer mockGameContainer = new EligibleGameContainer(BetCoefficient.FIRST_WIN, mockEligibleGames);
        BetProcessor betProcessor = new BetProcessor();
        betProcessor.process(mockGameContainer, true);
    }
}
