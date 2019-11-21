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
public class Repository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

//    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private File totalBetMadeFile;

    private Map<RuleNumber, File> totalRuleFile = new HashMap<>();

    public Repository(RuleNumber ruleNumber) {
        initializeFiles(ruleNumber);
    }

    private void initializeFiles(RuleNumber ruleNumber) {
        LocalDate date = LocalDate.now().plusDays(Day.TOMORROW.INDEX);
        String monthDirName = date.getMonth().name();
        totalBetMadeFile = new File(String.format("results/%s/BET_MADE_%s_%s.csv", monthDirName, ruleNumber, monthDirName));
        for (RuleNumber rule : RuleNumber.values()) {
            File totalRuleResultFile = new File(String.format("results/%s/%s.csv", monthDirName, "MATCHES_" + rule + "_" + monthDirName));
            totalRuleFile.put(rule, totalRuleResultFile);
        }
    }

    public List<Game> readTotalBetMadeFile() {
        return readFromFile(totalBetMadeFile);
    }

    /**
     * Read games from total_rule file
     * @param ruleNumber - number of rule.
     * @return - list of games.
     */
    public List<Game> readTotalRuleResultFile(RuleNumber ruleNumber) {
        return readFromFile(totalRuleFile.get(ruleNumber));
    }

    /**
     * Save games to total_bet_made file.
     * @param games - list of games.
     */
    public void saveTotalBetMadeGamesToFile(List<Game> games) {
        saveResultGamesToFile(totalBetMadeFile, games);
    }

    /**
     * Save games to total_rule file.
     * @param ruleNumber - number of rule.
     * @param games - list of games.
     */
    public void saveTotalRuleResultFile(RuleNumber ruleNumber, List<Game> games) {
        //TODO check correction
        writeToFile(totalRuleFile.get(ruleNumber), games);
    }

    private void saveResultGamesToFile(File file, List<Game> games) {
        List<Game> totalResultGames = readFromFile(file);
        totalResultGames.removeAll(games);
        totalResultGames.addAll(games);
        totalResultGames.sort(Comparator.comparing(Game::getDateTime));
        writeToFile(file, totalResultGames);
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
                        GameResult.valueOf(fields[12]), LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER));
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
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            final String GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";
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
                        game.getGameResult(),
                        DATE_FORMATTER.format(game.getParsingTime())) + "\n";
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
