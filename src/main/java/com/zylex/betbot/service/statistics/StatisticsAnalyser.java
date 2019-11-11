package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.StatisticsAnalyserConsoleLogger;
import com.zylex.betbot.exception.StatisticsAnalyserException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsAnalyser {

    private StatisticsAnalyserConsoleLogger logger = new StatisticsAnalyserConsoleLogger();

    private Repository repository;

    public StatisticsAnalyser(Repository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        Repository repository = new Repository(Day.TODAY, RuleNumber.RULE_ONE);
        new ResultScanner(repository).process();
        StatisticsAnalyser analyser = new StatisticsAnalyser(repository);
        LocalDate startDate = LocalDate.of(2019, 11, 5);
        LocalDate endDate = LocalDate.now().minusDays(1);
        analyser.analyse(startDate, endDate);
    }

    public void analyse(LocalDate startDate, LocalDate endDate) {
        try {
            logger.startLogMessage(startDate, endDate);
            List<Game> betMadeGames = filterByDate(startDate, endDate, repository.readTotalBetMadeFile());
            computeStatistics("Total", betMadeGames);
            List<Game> betMadeGamesByLeagues = getBetMadeGamesByLeagues(betMadeGames);
            computeStatistics("From file", betMadeGamesByLeagues);
        } catch (IOException e) {
            throw new StatisticsAnalyserException(e.getMessage(), e);
        }
    }

    private List<Game> getBetMadeGamesByLeagues(List<Game> betMadeGames) throws IOException {
        List<String> leagueLinksFromFile = readLeagueLinksFromFile();
        return betMadeGames.stream()
                .filter(game -> leagueLinksFromFile.contains(game.getLeagueLink())).collect(Collectors.toList());
    }

    private List<String> readLeagueLinksFromFile() throws IOException {
        List<String> leagueLinksFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("external-resources/leagues_list.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(leagueLinksFromFile::add);
        }
        return leagueLinksFromFile;
    }

    private List<Game> filterByDate(LocalDate startDate, LocalDate endDate, List<Game> betMadeGames) {
        betMadeGames = betMadeGames.stream()
                .filter(game -> {
                    LocalDate gameDate = game.getDateTime().toLocalDate();
                    return (!gameDate.isBefore(startDate) && !gameDate.isAfter(endDate));
                }).collect(Collectors.toList());
        return betMadeGames;
    }

    private void computeStatistics(String message, List<Game> games) {
        int firstWins = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.FIRST_WIN)).count();
        int ties = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.TIE)).count();
        int secondWins = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.SECOND_WIN)).count();
        int noResults = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.NO_RESULT)).count();
        logger.logStatistics(message, games.size(), firstWins, ties, secondWins, noResults);
    }
}
