package com.zylex.betbot.controller;

import com.zylex.betbot.exception.SaverException;
import com.zylex.betbot.model.Game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Process saving games into file.
 */
public class Saver {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    /**
     * Saves all games in "results" file.
     * @param games - list of games.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void processSaving(List<Game> games, String fileName) {
        try {
            File file = new File("results/" + fileName + ".csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writeToFile(games, writer);
            }
        } catch (IOException e) {
            throw new SaverException(e.getMessage(), e);
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
