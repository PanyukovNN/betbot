package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.BetRepository;
import com.zylex.betbot.exception.StatisticsAnalyserException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsAnalyser {

    private BetRepository betRepository;

    public StatisticsAnalyser(BetRepository betRepository) {
        this.betRepository = betRepository;
    }

    public static void main(String[] args) {
        StatisticsAnalyser analyser = new StatisticsAnalyser(new BetRepository(Day.TODAY, RuleNumber.RULE_ONE));
        analyser.analyse();
    }

    public void analyse() {
        try {
            LocalDate startDate = LocalDate.of(2019, 11, 5);
            LocalDate endDate = LocalDate.now().minusDays(0);

            List<Game> betMadeGames = betRepository.readTotalBetMadeFile();
            betMadeGames = betMadeGames.stream()
                    .filter(game -> {
                        LocalDate gameDate = game.getDateTime().toLocalDate();
                        return (!gameDate.isBefore(startDate) && !gameDate.isAfter(endDate));
                    }).collect(Collectors.toList());

            System.out.println(String.format("Statistics period from %s to %s", startDate, endDate));
            System.out.println("Total games: ");
            printStatistics(betMadeGames);

            System.out.println("Games from file: ");
            List<String> linksFromFile = getLinksFromFile();
            List<Game> betMadeGamesFromLeagueFile = betMadeGames.stream()
                    .filter(game -> linksFromFile.contains(game.getLeagueLink())).collect(Collectors.toList());
            printStatistics(betMadeGamesFromLeagueFile);
        } catch (IOException e) {
            throw new StatisticsAnalyserException(e.getMessage(), e);
        }
    }

    private void printStatistics(List<Game> games) {
        int firstWins = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.FIRST_WIN)).count();
        int ties = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.TIE)).count();
        int secondWins = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.SECOND_WIN)).count();
        int noResults = (int) games.stream().filter(game -> game.getGameResult().equals(GameResult.NO_RESULT)).count();

        System.out.print("Games number: " + games.size());
        if (noResults > 0) {
            System.out.print(String.format("; No result: %d", noResults));
        }
        System.out.print(String.format("\n1X: %d\n", firstWins));
        System.out.print(String.format(" X: %d\n", ties));
        System.out.print(String.format("X2: %d\n", secondWins));
        System.out.println(StringUtils.repeat("-", 30));
    }

    private List<String> getLinksFromFile() throws IOException {
        List<String> leagueLinksFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("external-resources/leagues_list.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(leagueLinksFromFile::add);
        }
        return leagueLinksFromFile;
    }
}
