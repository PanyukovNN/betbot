package com.zylex.betbot;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.*;
import com.zylex.betbot.service.bet.rule.Rule;
import com.zylex.betbot.service.bet.rule.RuleSaver;
import com.zylex.betbot.service.bet.rule.firstWinSecretRule;
import com.zylex.betbot.service.bet.rule.oneXSecretRule;
import com.zylex.betbot.service.parsing.ParseProcessor;

public class OneXBetBot {

    public static void main(String[] args) {
        Day day = Day.TOMORROW;
        int threads = Integer.parseInt(args[0]);
        DriverManager driverManager = new DriverManager(threads, true);
        ParseProcessor parseProcessor = new ParseProcessor();
        Repository repository = new Repository(day);
        Rule rule = new RuleSaver(new firstWinSecretRule(), repository);
        BetProcessor betProcessor = new BetProcessor(driverManager);

        EligibleGameContainer gameContainer = parseProcessor.process(driverManager, rule, day, repository, false);
        betProcessor.process(gameContainer, false);
    }

    /*private static void connectDb() {
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
    }*/
}
