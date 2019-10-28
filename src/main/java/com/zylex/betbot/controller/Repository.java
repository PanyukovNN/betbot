package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Process saving games into file.
 */
@SuppressWarnings("WeakerAccess")
public class Repository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String dirName;

    private Connection connection;

    public Repository(Day day) {
        createDirectory(day);
    }

    public void save(Game game) {
        try (PreparedStatement statement = connection.prepareStatement(SQLGame.INSERT.QUERY)) {
            statement.setString(1, game.getLeague());
            statement.setString(2, game.getLeagueLink());
            statement.setTimestamp(3, Timestamp.valueOf(game.getDateTime()));
            statement.setString(4, game.getFirstTeam());
            statement.setString(5, game.getSecondTeam());
            statement.setDouble(6, Double.parseDouble(game.getFirstWin()));
            statement.setDouble(7, Double.parseDouble(game.getTie()));
            statement.setDouble(8, Double.parseDouble(game.getSecondWin()));
            statement.setDouble(9, Double.parseDouble(game.getFirstWinOrTie()));
            statement.setDouble(10, Double.parseDouble(game.getSecondWinOrTie()));
            statement.setObject(11, null);
            statement.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public List<Game> readGamesFromFile(String fileName) {
        try {
            File file = new File(String.format("results/%s/%s.csv", dirName, fileName + dirName));
            List<String> lines = Files.readAllLines(file.toPath());
            List<Game> games = new ArrayList<>();
            for (String line : lines) {
                String[] fields = line.replace(",", ".").split(";");
                Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], fields[6], fields[7], fields[8], fields[9], fields[10]);
                games.add(game);
            }
            return games;
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    /**
     * Creates directory for concrete day.
     * @param day - day for parsing.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createDirectory(Day day) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        dirName = DIR_DATE_FORMATTER.format(date);
        new File("results").mkdir();
        new File("results/" + dirName).mkdir();
    }

    /**
     * Saves all games in "results" file.
     * @param games - list of games.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void processSaving(List<Game> games, String fileName) {
        try {
            File file = new File(String.format("results/%s/%s.csv", dirName, fileName + dirName));
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeToFile(games, writer);
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private void writeToFile(List<Game> games, BufferedWriter writer) throws IOException {
        final String GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";
        for (Game game : games) {
            String line = String.format(GAME_FORMAT,
                    game.getLeague(),
                    game.getLeagueLink(),
                    DATE_FORMATTER.format(game.getDateTime()),
                    game.getFirstTeam(),
                    game.getSecondTeam(),
                    formatDouble(game.getFirstWin()),
                    formatDouble(game.getTie()),
                    formatDouble(game.getSecondWin()),
                    formatDouble(game.getFirstWinOrTie()),
                    formatDouble(game.getSecondWinOrTie())) + "\n";
            writer.write(line);
        }
    }

    private String formatDouble(String value) {
        try {
            return new DecimalFormat("#.00").format(Double.parseDouble(value))
                    .replace('.', ',');
        } catch (NumberFormatException e) {
            return value;
        }
    }

    enum SQLGame {
        INSERT("INSERT INTO game (id, league, league_link, date_time, first_team, second_team, first_win, tie, second_win," +
                "first_win_or_tie, second_win_or_tie, result, recording_time) VALUES (DEFAULT, (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?), (?))");

        String QUERY;

        SQLGame(String QUERY) {
            this.QUERY = QUERY;
        }
    }
}
