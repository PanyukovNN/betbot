package com.zylex.betbot.controller;

import com.zylex.betbot.exception.BetRepositoryException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BetRepository extends Repository {

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

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
}
