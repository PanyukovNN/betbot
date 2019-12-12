package com.zylex.betbot.controller;

import com.zylex.betbot.exception.GameDaoException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.rule.RuleNumber;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GameDao {

    private final Connection connection;

    public GameDao(final Connection connection) {
        this.connection = connection;
    }

    public List<Game> getByRuleNumber(RuleNumber ruleNumber) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.GET_BY_RULE_NUMBER.QUERY)) {
            statement.setString(1, ruleNumber.toString());
            ResultSet resultSet = statement.executeQuery();
            List<Game> ruleGames = new ArrayList<>();
            while (resultSet.next()) {
                ruleGames.add(extractGame(resultSet));
            }
            return ruleGames;
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    public List<Game> getByDate(RuleNumber ruleNumber, LocalDate date) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.GET_BY_RULE_AND_DATE.QUERY)) {
            statement.setString(1, ruleNumber.toString());
            statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.of(date, LocalTime.MIN)));
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.of(date, LocalTime.MAX)));
            ResultSet resultSet = statement.executeQuery();
            List<Game> ruleGames = new ArrayList<>();
            while (resultSet.next()) {
                ruleGames.add(extractGame(resultSet));
            }
            return ruleGames;
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    private Game extractGame(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        String league = resultSet.getString("league");
        String leagueLink = resultSet.getString("league_link");
        LocalDateTime dateTime = resultSet.getTimestamp("date_time").toLocalDateTime();
        String firstTeam = resultSet.getString("first_team");
        String secondTeam = resultSet.getString("second_team");
        double firstWin = resultSet.getDouble("first_win");
        double tie = resultSet.getDouble("tie");
        double secondWin = resultSet.getDouble("second_win");
        double firstWinOrTie = resultSet.getDouble("first_win_or_tie");
        double secondWinOrTie = resultSet.getDouble("second_win_or_tie");
        Integer result = (Integer) resultSet.getObject("result");
        GameResult gameResult = intToGameResult(result);
        int betMade = resultSet.getInt("bet_made");
        return new Game(id, league, leagueLink, dateTime, firstTeam, secondTeam, firstWin, tie, secondWin, firstWinOrTie, secondWinOrTie, gameResult, betMade);
    }

    public void updateResult(Game game, RuleNumber ruleNumber) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.UPDATE_RESULT.QUERY)) {
            statement.setLong(1, game.getId());
            statement.setString(2, game.getLeague());
            statement.setString(3, game.getLeagueLink());
            statement.setTimestamp(4, Timestamp.valueOf(game.getDateTime()));
            statement.setString(5, game.getFirstTeam());
            statement.setString(6, game.getSecondTeam());
            statement.setDouble(7, game.getFirstWin());
            statement.setDouble(8, game.getTie());
            statement.setDouble(9, game.getSecondWin());
            statement.setDouble(10, game.getFirstWinOrTie());
            statement.setDouble(11, game.getSecondWinOrTie());
            statement.setObject(12, gameResultToInt(game.getGameResult()));
            statement.setInt(13, game.getBetMade());
            statement.setString(14, ruleNumber.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    public void save(Game game, RuleNumber ruleNumber) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.INSERT.QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, game.getLeague());
            statement.setString(2, game.getLeagueLink());
            statement.setTimestamp(3, Timestamp.valueOf(game.getDateTime()));
            statement.setString(4, game.getFirstTeam());
            statement.setString(5, game.getSecondTeam());
            statement.setDouble(6, game.getFirstWin());
            statement.setDouble(7, game.getTie());
            statement.setDouble(8, game.getSecondWin());
            statement.setDouble(9, game.getFirstWinOrTie());
            statement.setDouble(10, game.getSecondWinOrTie());
            statement.setObject(11, gameResultToInt(game.getGameResult()));
            statement.setInt(12, game.getBetMade());
            statement.setString(13, ruleNumber.toString());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                game.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    private GameResult intToGameResult(Integer result) {
        if (result == null) {
            return GameResult.NO_RESULT;
        } else if (result > 0) {
            return  GameResult.FIRST_WIN;
        } else if (result == 0) {
            return GameResult.TIE;
        } else if (result == -1) {
            return GameResult.SECOND_WIN;
        }
        return null;
    }

    private Integer gameResultToInt(GameResult gameResult) {
        Integer result;
        if (gameResult == GameResult.FIRST_WIN) {
            result = 1;
        } else if (gameResult == GameResult.TIE) {
            result = 0;
        } else if (gameResult == GameResult.SECOND_WIN) {
            result = -1;
        } else {
            result = null;
        }
        return result;
    }

    enum SQLGame {
        GET_BY_RULE_AND_DATE("SELECT * FROM game WHERE rule_number = (?) AND date_time >= (?) AND date_time <= (?)"),
        GET_BY_RULE_NUMBER("SELECT * FROM game WHERE rule_number = (?)"),
        INSERT("INSERT INTO game (id, league, league_link, date_time, first_team, second_team, first_win, tie, second_win, first_win_or_tie, second_win_or_tie, result, bet_made, rule_number) VALUES (DEFAULT, (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?))"),
        UPDATE_RESULT("INSERT INTO game (id, league, league_link, date_time, first_team, second_team, first_win, tie, second_win, first_win_or_tie, second_win_or_tie, result, bet_made, rule_number) VALUES ((?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?)) ON CONFLICT (id) DO UPDATE SET result = EXCLUDED.result");

        String QUERY;

        SQLGame(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
