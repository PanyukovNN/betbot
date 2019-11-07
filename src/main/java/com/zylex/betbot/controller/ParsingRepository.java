package com.zylex.betbot.controller;

import com.zylex.betbot.exception.RepositoryException;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Process saving games into file.
 */
public class ParsingRepository extends Repository {

    private Day day;

    private RuleProcessor ruleProcessor;

    public ParsingRepository(RuleProcessor ruleProcessor, Day day) {
        this.ruleProcessor = ruleProcessor;
        this.day = day;
    }

    /**
     * Saves all lists of games from GameContainer into separate files.
     */
    public GameContainer processSaving() {
        createDirectory(day);
        GameContainer gameContainer = ruleProcessor.process();
        try {
            saveParsedGameToFile("all_matches_", gameContainer.getAllGames());
            for (Map.Entry<RuleNumber, List<Game>> entry : gameContainer.getEligibleGames().entrySet()) {
                saveParsedGameToFile(String.format("matches_%s_", entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        return gameContainer;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveParsedGameToFile(String fileName, List<Game> games) throws IOException {
        File file = new File(String.format("results/%s/%s/%s.csv", monthDirName, dirName, fileName + dirName));
        if (!file.exists()) {
            file.createNewFile();
        }
        writeParsedGamesToFile(file, games);
    }
}
