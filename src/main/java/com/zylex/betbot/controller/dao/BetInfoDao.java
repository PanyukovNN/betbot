package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.BetInfoDaoException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Dao layer of bet dateTime.
 */
public class BetInfoDao {

    private final Connection connection;

    public BetInfoDao(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Get last bet dateTime from bet_info relation.
     * @return - instance of LocalDateTime.
     */
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

    /**
     * Save bet dateTime to bet_info relation.
     * @param betTime - instance of LocalDateTime.
     */
    public void save(LocalDateTime betTime) {
        try (PreparedStatement statement = connection.prepareStatement(SQLBet.INSERT.QUERY)) {
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
