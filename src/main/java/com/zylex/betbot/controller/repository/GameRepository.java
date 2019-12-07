package com.zylex.betbot.controller.repository;

import com.zylex.betbot.exception.GameRepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
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
     * Read games from RULE file.
     *
     * @param ruleNumber - number of rule.
     * @return - list of games.
     */
    public List<Game> readByRule(RuleNumber ruleNumber) {
        return readFromFile(ruleFileMap.get(ruleNumber));
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

    public void appendSaveByRule(RuleNumber ruleNumber, List<Game> games) {
        File ruleFile = ruleFileMap.get(ruleNumber);
        List<Game> resultGames = readFromFile(ruleFile);
        resultGames.removeAll(games);
        resultGames.addAll(games);
        writeToFile(ruleFile, resultGames);
    }

    private List<Game> readFromFile(File file) {
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(lines::add);
            List<Game> games = new ArrayList<>();
            for (String line : lines) {
                String[] fields = line.replace(",", ".").split(";");
                int betMade = 0;
                if (fields[11].equals("BET_MADE")) {
                    betMade = 1;
                } else if (fields[11].equals("ERROR")) {
                    betMade = -1;
                }
                Game game = new Game(0, fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], stringToDouble(fields[6]), stringToDouble(fields[7]), stringToDouble(fields[8]), stringToDouble(fields[9]), stringToDouble(fields[10]),
                        GameResult.valueOf(fields[12]), betMade);
                games.add(game);
            }
            return games;
        } catch (IOException e) {
            throw new GameRepositoryException(e.getMessage(), e);
        }
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

    private double stringToDouble(String value) {
        if (value.equals("-") || value.isEmpty()) {
            return 0d;
        } else {
            return Double.parseDouble(value);
        }
    }
}
