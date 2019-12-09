package com.zylex.betbot.controller.repository;

import com.zylex.betbot.exception.GameRepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Process saving and reading games from files.
 */
public class GameRepository extends Repository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private Map<RuleNumber, File> ruleFileMap = new HashMap<>();

    {
        for (RuleNumber rule : RuleNumber.values()) {
            File ruleFile = new File(String.format("results/MATCHES_%s.csv", rule));
            ruleFileMap.put(rule, ruleFile);
            createFile(ruleFile);
        }
    }

    /**
     * Save games to RULE file.
     *
     * @param ruleNumber - number of rule.
     * @param games      - list of games.
     */
    public void saveByRule(RuleNumber ruleNumber, List<Game> games) {
        writeToFile(ruleFileMap.get(ruleNumber), games);
    }

    private void writeToFile(File file, List<Game> games) {
        if (games.isEmpty()) {
            return;
        }
        games.sort(Comparator.comparing(Game::getDateTime));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            final String GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";
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
                        formatDouble(game.getSecondWinOrTie()),
                        game.getBetMade() > 0
                                ? "BET_MADE"
                                : game.getBetMade() < 0
                                ? "ERROR"
                                : "-",
                        game.getGameResult()) + "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            throw new GameRepositoryException(e.getMessage(), e);
        }
    }

    private String formatDouble(double value) {
        try {
            return new DecimalFormat("#.00").format(value)
                    .replace('.', ',');
        } catch (NumberFormatException e) {
            return "-";
        }
    }
}
