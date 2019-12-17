package com.zylex.betbot;

import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.dao.LeagueDao;
import com.zylex.betbot.exception.BetBotException;
import com.zylex.betbot.exception.StatisticsApplicationException;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

public class StatisticsApplication {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2019, 12, 9);
        LocalDate endDate = LocalDate.now().minusDays(1);

        try (Connection connection = getConnection()) {
            new StatisticsAnalyser(
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
}
