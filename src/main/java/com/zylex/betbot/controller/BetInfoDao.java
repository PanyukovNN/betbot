package com.zylex.betbot.controller;

import com.zylex.betbot.exception.BetInfoDaoException;

import java.sql.*;
import java.time.LocalDateTime;

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
            return null;
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
