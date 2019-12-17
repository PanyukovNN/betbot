package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.BetInfoDaoException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BetInfoDao {

    private final Connection connection;

    public BetInfoDao(final Connection connection) {
        this.connection = connection;
    }

    public LocalDateTime getLast() {
        try (PreparedStatement statement = connection.prepareStatement(SQLBet.GET_LAST.QUERY)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getTimestamp("bet_time").toLocalDateTime();
            }
            return LocalDateTime.of(LocalDate.now().minusDays(2), LocalTime.of(0, 0));
        } catch (SQLException e) {
            throw new BetInfoDaoException(e.getMessage(), e);
        }
    }

    public void save(LocalDateTime betTime) {
        try (PreparedStatement statement = connection.prepareStatement(SQLBet.INSERT.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setTimestamp(1, Timestamp.valueOf(betTime));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new BetInfoDaoException(e.getMessage(), e);
        }
    }

    enum SQLBet {
        GET_LAST("SELECT * FROM bet_info ORDER BY id DESC LIMIT 1"),
        INSERT("INSERT INTO bet_info (id, bet_time) VALUES (DEFAULT, (?))");

        String QUERY;

        SQLBet(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
