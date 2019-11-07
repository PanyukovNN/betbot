package com.zylex.betbot.controller;

import com.zylex.betbot.exception.BetRepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BetRepository {

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private String monthDirName;

    private File betMadeFile;

    public BetRepository(Day day) {
        LocalDate date = LocalDate.now().plusDays(day.INDEX);
        monthDirName = date.getMonth().name();
        String dirName = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(date);
        betMadeFile = new File(String.format("results/%s/%s/BET_MADE_%s.csv", monthDirName, dirName, dirName));
    }

    public List<Game> readBetMadeFile() {
        return processReadBetMade(betMadeFile);
    }

    private List<Game> processReadBetMade(File file) {
        try {
            List<Game> betMadeGames = new ArrayList<>();
            if (file.createNewFile()) {
                return betMadeGames;
            }
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] fields = line.split(";");
                Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], GameResult.NO_RESULT);
                String[] rules = fields[6].split("__");
                Set<RuleNumber> ruleNumberSet = game.getRuleNumberSet();
                for (String rule : rules) {
                    ruleNumberSet.add(RuleNumber.valueOf(rule));
                }
                if (game.getDateTime().isAfter(LocalDateTime.now().minusDays(1))) {
                    betMadeGames.add(game);
                }
            }
            return betMadeGames;
        } catch (IOException e) {
            throw new BetRepositoryException(e.getMessage(), e);
        }
    }

    public void saveBetMadeGamesToFile(List<Game> betMadeGames) throws IOException {
        writeBetMadeGamesToFile(betMadeFile, betMadeGames);
    }

    public void saveTotalBetGamesToFile(List<Game> betMadeGames) throws IOException {
        File totalBetsMadeFile = new File(String.format("results/%s/BET_MADE_%s.csv", monthDirName, monthDirName));
        List<Game> totalBetMadeGames = processReadBetMade(totalBetsMadeFile);
        totalBetMadeGames.removeAll(betMadeGames);
        totalBetMadeGames.addAll(betMadeGames);
        totalBetMadeGames.sort(Comparator.comparing(Game::getDateTime));
        writeBetMadeGamesToFile(totalBetsMadeFile, totalBetMadeGames);
    }

    private void writeBetMadeGamesToFile(File file, List<Game> madeBetsGames) throws IOException {
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
}
