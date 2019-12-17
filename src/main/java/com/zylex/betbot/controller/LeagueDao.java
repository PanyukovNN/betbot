package com.zylex.betbot.controller;

import com.zylex.betbot.exception.LeagueDaoException;
import com.zylex.betbot.service.rule.RuleNumber;

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

    public List<String> getExcludeLeagues(RuleNumber ruleNumber) {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.GET_EXCLUDE_LEAGUES_BY_RULE.QUERY)) {
            statement.setString(1, ruleNumber.toString());
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

    public void saveSelectedLeague(String leagueLink) {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.INSERT_SELECTED_LEAGUE.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leagueLink);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new LeagueDaoException(e.getMessage(), e);
        }
    }

    public void saveExcludeLeague(String leagueLink, RuleNumber ruleNumber) {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.INSERT_EXCLUDE_LEAGUE.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leagueLink);
            statement.setString(2, ruleNumber.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new LeagueDaoException(e.getMessage(), e);
        }
    }

    enum SQLLeague {
        GET_ALL_SELECTED_LEAGUES("SELECT * FROM selected_league"),
        GET_EXCLUDE_LEAGUES_BY_RULE("SELECT * FROM exclude_league WHERE rule_number = (?)"),
        INSERT_SELECTED_LEAGUE("INSERT INTO selected_league (id, league_link) VALUES (DEFAULT, (?))"),
        INSERT_EXCLUDE_LEAGUE("INSERT INTO exclude_league (id, league_link, rule_number) VALUES (DEFAULT, (?), (?))");

        String QUERY;

        SQLLeague(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
