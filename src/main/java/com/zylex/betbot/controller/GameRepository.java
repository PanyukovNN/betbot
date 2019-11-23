package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Process saving and reading games from files.
 */
public class GameRepository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private Map<RuleNumber, File> betMadeFileMap = new HashMap<>();

    private Map<RuleNumber, File> ruleFileMap = new HashMap<>();

    {
        LocalDate date = LocalDate.now().plusDays(Day.TOMORROW.INDEX);
        String monthDirName = date.getMonth().name();
        for (RuleNumber rule : RuleNumber.values()) {
            betMadeFileMap.put(rule, new File(String.format("results/%s/BET_MADE_%s_%s.csv", monthDirName, rule, monthDirName)));
            ruleFileMap.put(rule, new File(String.format("results/%s/MATCHES_%s_%s.csv", monthDirName, rule, monthDirName)));
        }
    }

    /**
     * Read games from BET_MADE file.
     * @return list of games.
     */
    public List<Game> readBetMade(RuleNumber ruleNumber) {
        return readFromFile(betMadeFileMap.get(ruleNumber));
    }

    /**
     * Read games from RULE file.
     * @param ruleNumber - number of rule.
     * @return - list of games.
     */
    public List<Game> readByRule(RuleNumber ruleNumber) {
        return readFromFile(ruleFileMap.get(ruleNumber));
    }

    /**
     * Save games to BET_MADE file.
     * @param games - list of games.
     */
    public void saveBetMade(RuleNumber ruleNumber, List<Game> games) {
        appendSave(betMadeFileMap.get(ruleNumber), games);
    }

    /**
     * Save games to RULE file.
     * @param ruleNumber - number of rule.
     * @param games - list of games.
     */
    public void saveByRule(RuleNumber ruleNumber, List<Game> games) {
        writeToFile(ruleFileMap.get(ruleNumber), games);
    }

    private void appendSave(File file, List<Game> games) {
        List<Game> resultGames = readFromFile(file);
        resultGames.removeAll(games);
        resultGames.addAll(games);
        writeToFile(file, resultGames);
    }

    private List<Game> readFromFile(File file) {
        if (!file.exists()) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(lines::add);
            List<Game> games = new ArrayList<>();
            for (String line : lines) {
                String[] fields = line.replace(",", ".").split(";");
                Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], fields[6], fields[7], fields[8], fields[9], fields[10],
                        GameResult.valueOf(fields[12]));
                if (!fields[11].equals("-")) {
                    String[] rules = fields[11].split("__");
                    game.getRuleNumberSet().addAll(
                            Arrays.stream(rules).map(RuleNumber::valueOf)
                                    .collect(Collectors.toList()));
                }
                games.add(game);
            }
            return games;
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    private void writeToFile(File file, List<Game> games) {
        if (games.isEmpty()) {
            return;
        }
        createFile(file);
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
                        game.getRuleNumberSet().isEmpty()
                                ? "-"
                                : StringUtils.join(game.getRuleNumberSet(), "__"),
                        game.getGameResult()) + "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
}
