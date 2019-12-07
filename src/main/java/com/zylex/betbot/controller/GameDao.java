package com.zylex.betbot.controller;

import com.zylex.betbot.exception.GameDaoException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameDao {

    private final Connection connection;

    public GameDao(final Connection connection) {
        this.connection = connection;
    }

    public Game getById(int id) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.GET_BY_ID.QUERY)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
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
            return new Game(0, "", "", LocalDateTime.now(), "", "", 0, 0, 0, 0, 0, GameResult.NO_RESULT, 0);
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    public List<Game> getAll() {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.GET_ALL.QUERY)) {
            ResultSet resultSet = statement.executeQuery();
            List<Game> games = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
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
                games.add(new Game(id, league, leagueLink, dateTime, firstTeam, secondTeam, firstWin, tie, secondWin, firstWinOrTie, secondWinOrTie, gameResult, betMade));
            }
            return games;
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    public Game save(Game game) {
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
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                game.setId(generatedKeys.getInt(1));
            }
            return game;
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.DELETE_BY_ID.QUERY)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new GameDaoException(e.getMessage(), e);
        }
    }

    private GameResult intToGameResult(Integer result) {
        GameResult gameResult = GameResult.NO_RESULT;
        if (result > 0) {
            gameResult = GameResult.FIRST_WIN;
        } else if (result == 0) {
            gameResult = GameResult.TIE;
        } else if (result == -1) {
            gameResult = GameResult.SECOND_WIN;
        }
        return gameResult;
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
        GET_ALL("SELECT * FROM game"),
        GET_BY_ID("SELECT id, league, league_link, date_time, first_team, second_team, first_win, tie, second_win, first_win_or_tie, second_win_or_tie, result FROM game WHERE id = (?)"),
        INSERT("INSERT INTO game (id, league, league_link, date_time, first_team, second_team, first_win, tie, second_win, first_win_or_tie, second_win_or_tie, result) VALUES (DEFAULT, (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?))"),
        DELETE_BY_ID("DELETE FROM game WHERE id = (?)");

        String QUERY;

        SQLGame(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
