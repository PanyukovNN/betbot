package com.zylex.betbot;

import com.zylex.betbot.controller.GameDao;
import com.zylex.betbot.controller.repository.BalanceRepository;
import com.zylex.betbot.controller.repository.BetInfoRepository;
import com.zylex.betbot.controller.repository.GameRepository;
import com.zylex.betbot.controller.repository.LeagueRepository;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.exception.BetBotException;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.rule.RuleNumber;
import com.zylex.betbot.service.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class BetBotApplication {

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            new BetProcessor(
                new RuleProcessor(
                    new GameRepository(),
                    new LeagueRepository(),
                    new ParseProcessor()
                ),
                new BetInfoRepository(),
                new BalanceRepository(),
                new GameDao(connection),
                defineRuleNumbers(args)
            ).process();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }

    private static Connection getConnection() {
        try(FileInputStream inputStream = new FileInputStream("src/main/resources/BetBotDb.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            final String login = property.getProperty("db.login");
            final String password = property.getProperty("db.password");
            final String url = property.getProperty("db.url");
            Class.forName("org.postgresql.Driver");
            return java.sql.DriverManager.getConnection(url, login, password);
        } catch(SQLException | IOException | ClassNotFoundException e) {
            throw new BetBotException(e.getMessage(), e);
        }
    }

    private static List<RuleNumber> defineRuleNumbers(String[] args) {
        List<RuleNumber> ruleNumbers = new ArrayList<>();
        if (args.length > 0) {
            if (Arrays.asList(args).contains("-1")) {
                ruleNumbers.add(RuleNumber.RULE_ONE);
            }
            if (Arrays.asList(args).contains("-2")) {
                ruleNumbers.add(RuleNumber.RULE_TEST);
            }
        }
        return ruleNumbers;
    }
}
