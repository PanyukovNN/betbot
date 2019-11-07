package com.zylex.betbot.controller;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResultRepository {

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private File betMadeFile = new File(String.format("results/%s/BET_MADE_%s.csv", LocalDate.now().getMonth().name(), LocalDate.now().getMonth().name()));

    public List<Game> readResultGames() throws IOException {
        List<Game> betMadeGames = new ArrayList<>();
        if (betMadeFile.createNewFile()) {
            return betMadeGames;
        }
        List<String> lines = Files.readAllLines(betMadeFile.toPath());
        for (String line : lines) {
            String[] fields = line.split(";");
            String league = fields[0];
            String leagueLink = fields[1];
            LocalDateTime dateTime = LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER);
            String firstTeam = fields[4];
            String secondTeam = fields[5];
            GameResult gameResult = GameResult.NO_RESULT;
            if (fields.length > 7) {
                gameResult = GameResult.valueOf(fields[7]);
            }
            Game game = new Game(league, leagueLink, dateTime, firstTeam, secondTeam, gameResult);
            Set<RuleNumber> ruleNumberSet = game.getRuleNumberSet();
            String[] rules = fields[6].split("__");
            for (String rule : rules) {
                ruleNumberSet.add(RuleNumber.valueOf(rule));
            }
            betMadeGames.add(game);
        }
        return betMadeGames;
    }

    public void saveResultGamesToFile(List<Game> madeBetsGames) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(betMadeFile), StandardCharsets.UTF_8))) {
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
