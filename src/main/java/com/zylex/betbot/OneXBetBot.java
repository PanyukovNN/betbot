package com.zylex.betbot;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.oneXSecretRule;
import com.zylex.betbot.service.bet.Rule;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class OneXBetBot {

    private static Connection connection;

    public static void main(String[] args) throws SQLException {
        int threads = Integer.parseInt(args[0]);
        Day day = Day.TODAY;
        ParseProcessor parseProcessor = new ParseProcessor();
        Repository repository = new Repository(day);
        Rule rule = new oneXSecretRule();
//        List<Game> games = parseProcessor.process(threads, day);
        List<Game> games = repository.readGamesFromFile("all_matches_");
        repository.processSaving(games, "all_matches_");
        EligibleGameContainer gameContainer = rule.filter(games);
        ConsoleLogger.writeInLine("\nEligible games: " + ConsoleLogger.eligibleGames);
        repository.processSaving(gameContainer.getEligibleGames(), "eligible_matches_one_x_");

//        ConsoleLogger.writeLineSeparator();
//        ConsoleLogger.writeInLine("\n");
//        List<Game> mockEligibleGames = repository.readGamesFromFile("eligible_matches_");
//        EligibleGameContainer mockGameContainer = new EligibleGameContainer(BetCoefficient.FIRST_WIN, mockEligibleGames);
//        BetProcessor betProcessor = new BetProcessor();
//        betProcessor.process(mockGameContainer, true);
    }

    private static void connectDb() {
        try(FileInputStream inputStream = new FileInputStream("src/main/resources/oneXBetDb.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            final String login = property.getProperty("db.login");
            final String password = property.getProperty("db.password");
            final String url = property.getProperty("db.url");
            Class.forName("org.postgresql.Driver");
            connection = java.sql.DriverManager.getConnection(url, login, password);
        } catch(SQLException | IOException | ClassNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
}
