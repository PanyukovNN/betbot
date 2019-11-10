package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Process saving games into file.
 */
public class ParsingRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String dirName;

    private String monthDirName;

    private boolean directoryCreated = false;

    public List<Game> readGamesFromFile(Day day) {
        try {
            if (!directoryCreated) {
                createDirectory(day);
            }
            File file = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, "all_matches_" + dirName));
            List<String> lines = new ArrayList<>();
            try (InputStream inputStream = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(lines::add);
            }
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
     * Saves all lists of games from GameContainer into separate files.
     */
    public void saveGamesToFiles(Day day, GameContainer gameContainer) {
        try {
            if (!directoryCreated) {
                createDirectory(day);
            }
            saveParsedGameToFile("all_matches_", gameContainer.getAllGames());
            for (Map.Entry<RuleNumber, List<Game>> entry : gameContainer.getEligibleGames().entrySet()) {
                saveParsedGameToFile(String.format("matches_%s_", entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectory(Day day) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        monthDirName = date.getMonth().name();
        dirName = DIR_DATE_FORMATTER.format(date);
        new File(String.format("results/%s/%s", monthDirName, dirName)).mkdirs();
        directoryCreated = true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveParsedGameToFile(String fileName, List<Game> games) throws IOException {
        File file = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, fileName + dirName));
        file.createNewFile();
        writeParsedGamesToFile(file, games);
    }

    private void writeParsedGamesToFile(File file, List<Game> games) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
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
