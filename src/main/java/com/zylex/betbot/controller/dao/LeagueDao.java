package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.LeagueDaoException;
import com.zylex.betbot.service.rule.RuleNumber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dao layer of league.
 */
public class LeagueDao {

    private final Connection connection;

    public LeagueDao(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Get list of all leagues links from selected_league relation.
     * @return - list of leagues links.
     */
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

    /**
     * Get list of leagues links from exclude_league relation by rule number.
     * @param ruleNumber - number of rule.
     * @return - list of leagues links.
     */
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

    /**
     * Save league link to selected_league relation.
     * @param leagueLink - league link.
     */
    public void saveSelectedLeague(String leagueLink) {
        try (PreparedStatement statement = connection.prepareStatement(SQLLeague.INSERT_SELECTED_LEAGUE.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, leagueLink);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new LeagueDaoException(e.getMessage(), e);
        }
    }

    /**
     * Saves league link to exclude_league relation
     * @param leagueLink - league link.
     * @param ruleNumber - number of rule.
     */
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
