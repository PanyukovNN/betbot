package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Process saving games into file.
 */
@SuppressWarnings("WeakerAccess")
public class Repository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static String dirName;

    public Repository(Day day) {
        createDirectory(day);
    }

    /**
     * Reading all games from specified file, and return them.
     * @param fileName - name of the file.
     * @return - list of games.
     */
    public static List<Game> readGamesFromFile(String fileName) {
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
}
