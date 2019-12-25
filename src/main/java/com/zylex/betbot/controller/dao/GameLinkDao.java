package com.zylex.betbot.controller.dao;

import com.zylex.betbot.exception.GameLinkDaoException;
import com.zylex.betbot.model.Game;

import java.sql.*;

/**
 * Dao layer of game_link relation.
 */
@SuppressWarnings("WeakerAccess")
public class GameLinkDao {

    private final Connection connection;

    public GameLinkDao(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Get link of the game from game_link relation by game id.
     * @param gameId - id of game.
     * @return - game link.
     */
    public String getByGameId(long gameId) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGameLink.GET_BY_ID.QUERY)) {
            statement.setLong(1, gameId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("link");
            }
            return "";
        } catch (SQLException e) {
            throw new GameLinkDaoException(e.getMessage(), e);
        }
    }

    /**
     * Saves link of the game to game_link relation.
     * @param game - instance of game.
     */
    public void save(Game game) {
        if (!getByGameId(game.getId()).isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(SQLGameLink.INSERT.QUERY)) {
            statement.setLong(1, game.getId());
            statement.setString(2, game.getLink());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new GameLinkDaoException(e.getMessage(), e);
        }
    }

    enum SQLGameLink {
        GET_BY_ID("SELECT * FROM game_link WHERE game_id = (?)"),
        INSERT("INSERT INTO game_link (game_id, link) VALUES ((?), (?))");

        String QUERY;

        SQLGameLink(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
