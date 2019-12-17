package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.BankDaoException;

import java.sql.*;
import java.time.LocalDate;

public class BankDao {

    private final Connection connection;

    public BankDao(final Connection connection) {
        this.connection = connection;
    }

    public int getLast() {
        try (PreparedStatement statement = connection.prepareStatement(SQLBank.GET_LAST.QUERY)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("amount");
            }
            return 0;
        } catch (SQLException e) {
            throw new BankDaoException(e.getMessage(), e);
        }
    }

    public void save(int amount) {
        try (PreparedStatement statement = connection.prepareStatement(SQLBank.INSERT.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, amount);
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new BankDaoException(e.getMessage(), e);
        }
    }

    enum SQLBank {
        GET_LAST("SELECT * FROM bank ORDER BY id DESC LIMIT 1"),
        INSERT("INSERT INTO bank (id, amount, bank_date) VALUES (DEFAULT, (?), (?))");

        String QUERY;

        SQLBank(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
