package com.zylex.betbot.controller;

import com.zylex.betbot.exception.LeagueDaoException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeagueDao {

    private final Connection connection;

    public LeagueDao(final Connection connection) {
        this.connection = connection;
    }

    public List<String> getAllSelectedLeagues() {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.GET_ALL_SELECTED_LEAGUES.QUERY)) {
            ResultSet resultSet = statement.executeQuery();
            List<String> selectedLeagues = new ArrayList<>();
            while (resultSet.next()) {
                String leagueLink = resultSet.getString("league_link");
                selectedLeagues.add(leagueLink);
            }
            return selectedLeagues;
        } catch (SQLException e) {
            throw new LeagueDaoException(e.getMessage(), e);
        }
    }

    public void save(String leagueLink) {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.INSERT_SELECTED_LEAGUE.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leagueLink);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new LeagueDaoException(e.getMessage(), e);
        }
    }

    enum SQLLeague {
        GET_ALL_SELECTED_LEAGUES("SELECT * FROM selected_league"),
        INSERT_SELECTED_LEAGUE("INSERT INTO selected_league (id, league_link) VALUES (DEFAULT, (?))");

        String QUERY;

        SQLLeague(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
