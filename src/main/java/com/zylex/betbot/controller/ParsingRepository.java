package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Process saving games into file.
 */
public class ParsingRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Day day;

    private RuleProcessor ruleProcessor;

    private String dirName;

    private String monthDirName;

    public ParsingRepository(RuleProcessor ruleProcessor, Day day) {
        this.ruleProcessor = ruleProcessor;
        this.day = day;
    }

    /**
     * Creates directory for concrete day.
     * @param day - day for parsing.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createDirectory(Day day) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        monthDirName = date.getMonth().name();
        dirName = DIR_DATE_FORMATTER.format(date);
        new File(String.format("results/%s/%s", monthDirName, dirName)).mkdirs();
    }

    /**
     * Saves all lists of games from GameContainer into separate files.
     */
    public GameContainer processSaving() {
        GameContainer gameContainer = ruleProcessor.process();
        try {
            createDirectory(day);
            saveParsedGameToFile("all_matches_", gameContainer.getAllGames());
            for (Map.Entry<RuleNumber, List<Game>> entry : gameContainer.getEligibleGames().entrySet()) {
                saveParsedGameToFile(String.format("matches_%s_", entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        return gameContainer;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveParsedGameToFile(String fileName, List<Game> games) throws IOException {
        File file = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, fileName + dirName));
        if (!file.exists()) {
            file.createNewFile();
        }
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
