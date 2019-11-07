package com.zylex.betbot.controller;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

abstract class Repository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    static String dirName;

    static String monthDirName;

    static File betMadeFile;

    /**
     * Creates directory for concrete day.
     * @param day - day for parsing.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void createDirectory(Day day) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        monthDirName = date.getMonth().name();
        dirName = DIR_DATE_FORMATTER.format(date);
        new File("results").mkdir();
        new File("results/" + monthDirName).mkdir();
        new File("results/" + monthDirName + "/" + dirName).mkdir();
        betMadeFile = new File(String.format("results/%s/%s/BET_MADE_%s.csv", monthDirName, dirName, dirName));
    }

    void writeBetMadeGamesToFile(File file, List<Game> madeBetsGames) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            String MADE_BET_GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s\n";
            for (Game game : madeBetsGames) {
                String line = String.format(MADE_BET_GAME_FORMAT,
                        game.getLeague(),
                        game.getLeagueLink(),
                        DATE_FORMATTER.format(game.getDateTime()),
                        game.getFirstTeam(),
                        game.getSecondTeam(),
                        StringUtils.join(game.getRuleNumberSet(), "__"),
                        game.getGameResult());
                writer.write(line);
            }
        }
    }

    void writeParsedGamesToFile(File file, List<Game> games) throws IOException {
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
