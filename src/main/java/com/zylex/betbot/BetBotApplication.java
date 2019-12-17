package com.zylex.betbot;

import com.zylex.betbot.controller.dao.BankDao;
import com.zylex.betbot.controller.dao.BetInfoDao;
import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.dao.LeagueDao;
import com.zylex.betbot.controller.logger.ConsoleLogger;
import com.zylex.betbot.exception.BetBotException;
import com.zylex.betbot.service.bet.*;

import com.zylex.betbot.service.rule.RuleNumber;
import com.zylex.betbot.service.rule.RuleProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class BetBotApplication {

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {
            new BetProcessor(
                new RuleProcessor(
                    new ParseProcessor(),
                    new LeagueDao(connection),
                    new GameDao(connection),
                    new BetInfoDao(connection)
                ),
                new BankDao(connection),
                defineRuleNumbers(args)
            ).process();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConsoleLogger.writeToLogFile();
        }
    }

    private static Connection getConnection() {
        try(InputStream inputStream = BetBotApplication.class.getClassLoader().getResourceAsStream("BetBotDb.properties")) {
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
