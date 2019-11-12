package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Process saving games into file.
 */
public class Repository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private File allMatchesFile;

    private File betMadeFile;

    private File totalBetMadeFile;

    private Map<RuleNumber, File> ruleFile = new HashMap<>();

    private Map<RuleNumber, File> totalRuleFile = new HashMap<>();

    private Day day;

    public Repository(Day day, RuleNumber ruleNumber) {
        this.day = day;
        createFiles(day, ruleNumber);
    }

    public Day getDay() {
        return day;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createFiles(Day day, RuleNumber ruleNumber) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        String monthDirName = date.getMonth().name();
        String dirName = DIR_DATE_FORMATTER.format(date);
        new File(String.format("results/%s/%s", monthDirName, dirName)).mkdirs();
        allMatchesFile = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, "all_matches_" + dirName));
        betMadeFile = new File(String.format("results/%s/%s/BET_MADE_%s_%s.csv", monthDirName, dirName, ruleNumber, dirName));
        totalBetMadeFile = new File(String.format("results/%s/BET_MADE_%s_%s.csv", monthDirName, ruleNumber, monthDirName));
        for (RuleNumber rule : RuleNumber.values()) {
            File totalRuleResultFile = new File(String.format("results/%s/%s.csv", monthDirName, "MATCHES_" + rule + "_" + monthDirName));
            totalRuleFile.put(rule, totalRuleResultFile);

            File ruleResultFile = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, "matches_" + rule + "_" + dirName));
            ruleFile.put(rule, ruleResultFile);
        }
    }

    public List<Game> readAllMatchesFile() {
        return processReadGames(allMatchesFile);
    }

    private List<Game> processReadGames(File file) {
        try {
            List<Game> games = new ArrayList<>();
            if (file.createNewFile()) {
                return games;
            }
            List<String> lines = new ArrayList<>();
            try (InputStream inputStream = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(lines::add);
            }
            for (String line : lines) {
                String[] fields = line.replace(",", ".").split(";");
                Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], fields[6], fields[7], fields[8], fields[9], fields[10],
                        LocalDateTime.parse(fields[11] + ";" + fields[12], DATE_FORMATTER));
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
    public void saveGamesToFiles(GameContainer gameContainer) {
        try {
            writeParsedGamesToFile(allMatchesFile, gameContainer.getAllGames());
            for (Map.Entry<RuleNumber, List<Game>> entry : gameContainer.getEligibleGames().entrySet()) {
                writeParsedGamesToFile(ruleFile.get(entry.getKey()), entry.getValue());
                saveResultGamesToFile(totalRuleFile.get(entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeParsedGamesToFile(File file, List<Game> games) throws IOException {
        file.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            final String GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s";
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
                        DATE_FORMATTER.format(game.getParsingTime())) + "\n";
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

    public List<Game> readBetMadeFile() {
        return processReadResultFile(betMadeFile);
    }

    public List<Game> readTotalRuleResultFile(RuleNumber ruleNumber) {
        return processReadResultFile(totalRuleFile.get(ruleNumber));
    }

    private List<Game> processReadResultFile(File file) {
        try {
            List<Game> betMadeGames = new ArrayList<>();
            if (file.createNewFile()) {
                return betMadeGames;
            }
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] fields = line.split(";");
                Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], GameResult.valueOf(fields[7]));
                String[] rules = fields[6].split("__");
                Set<RuleNumber> ruleNumberSet = game.getRuleNumberSet();
                for (String rule : rules) {
                    ruleNumberSet.add(RuleNumber.valueOf(rule));
                }
                betMadeGames.add(game);
            }
            return betMadeGames;
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void saveBetMadeGamesToFile(List<Game> betMadeGames) throws IOException {
        writeResultGamesToFile(betMadeFile, betMadeGames);
    }

    public void saveTotalBetMadeGamesToFile(List<Game> games) throws IOException {
        saveResultGamesToFile(totalBetMadeFile, games);
    }

    public void saveTotalRuleResultFile(RuleNumber ruleNumber, List<Game> games) throws IOException {
        saveResultGamesToFile(totalRuleFile.get(ruleNumber), games);
    }

    private void saveResultGamesToFile(File file, List<Game> games) throws IOException {
        List<Game> totalResultGames = processReadResultFile(file);
        totalResultGames.removeAll(games);
        totalResultGames.addAll(games);
        totalResultGames.sort(Comparator.comparing(Game::getDateTime));
        writeResultGamesToFile(file, totalResultGames);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeResultGamesToFile(File file, List<Game> betMadeGames) throws IOException {
        file.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            String MADE_BET_GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s\n";
            for (Game game : betMadeGames) {
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
}
