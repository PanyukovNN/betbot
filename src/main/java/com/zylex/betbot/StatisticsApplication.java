package com.zylex.betbot;

import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.dao.GameLinkDao;
import com.zylex.betbot.controller.dao.LeagueDao;
import com.zylex.betbot.exception.BetBotException;
import com.zylex.betbot.exception.StatisticsApplicationException;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsCollector;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

public class StatisticsApplication {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2019, 12, 16);
        LocalDate endDate = LocalDate.now().minusDays(0);

        try (Connection connection = getConnection()) {
            new StatisticsCollector(
                new ResultScanner(
                    new GameDao(connection)
                ),
                new LeagueDao(connection)
            ).analyse(startDate, endDate);
        } catch (SQLException e) {
            throw new StatisticsApplicationException(e.getMessage(), e);
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
}
