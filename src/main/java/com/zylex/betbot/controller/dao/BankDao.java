package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.BankDaoException;

import java.sql.*;
import java.time.LocalDate;

/**
 * Dao layer of bank.
 */
public class BankDao {

    private final Connection connection;

    public BankDao(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Get last record in bank relation.
     * @return - last amount.
     */
    public int getMax() {
        try (PreparedStatement statement = connection.prepareStatement(SQLBank.GET_LAST.QUERY)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new BankDaoException(e.getMessage(), e);
        }
    }

    /**
     * Save amount to bank.
     * @param amount - bank amount.
     */
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
        GET_LAST("SELECT MAX(amount) FROM bank"),
        INSERT("INSERT INTO bank (id, amount, bank_date) VALUES (DEFAULT, (?), (?))");

        String QUERY;

        SQLBank(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
