package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.GameBotException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.Day;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Thread for parsing one league link.
 */
@SuppressWarnings("WeakerAccess")
public class CallableGameParser implements Callable<List<Game>> {

    private ParsingConsoleLogger logger;

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private String leagueLink;

    private LocalDateTime parsingTime;

    public CallableGameParser(ParsingConsoleLogger logger, String leagueLink, LocalDateTime parsingTime) {
        this.logger = logger;
        this.leagueLink = leagueLink;
        this.parsingTime = parsingTime;
    }

    /**
     * Parsing by jsoup league link on site and return all matches for specified day.
     * @return - list of games.
     */
    @Override
    public List<Game> call() {
        try {
            return processGameParsing();
        } catch (HttpStatusException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            throw new GameBotException(e.getMessage(), e);
        }
    }

    private List<Game> processGameParsing() throws IOException {
        logger.logLeagueGame();
        Document document = Jsoup.connect(String.format("https://1xstavka.ru/line/Football/%s", leagueLink))
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
        return parseGames(document);
    }

    private List<Game> parseGames(Document document) {
        List<Game> games = new ArrayList<>();
        String leagueName = document.select("span.c-events__liga").text();
        Elements gameElements = document.select("div.c-events__item_game");
        LocalDate today = LocalDate.now().plusDays(Day.TODAY.INDEX);
        LocalDate tomorrow = LocalDate.now().plusDays(Day.TOMORROW.INDEX);
        for (Element gameElement : gameElements) {
            LocalDateTime dateTime = processDate(gameElement).plusHours(3);
            if (dateTime.toLocalDate().isBefore(today)) {
                continue;
            } else if (dateTime.toLocalDate().isAfter(tomorrow)) {
                break;
            }
            Elements teams = gameElement.select("span.c-events__team");
            if (teams.isEmpty()) {
                continue;
            }
            String firstTeam = teams.get(0).text();
            String secondTeam = teams.get(1).text();
            if (firstTeam.contains("(голы)")) {
                continue;
            }
            Elements coefficients = gameElement.select("div.c-bets > a.c-bets__bet");
            String firstWin = coefficients.get(0).text();
            String tie = coefficients.get(1).text();
            String secondWin = coefficients.get(2).text();
            String firstWinOrTie = coefficients.get(3).text();
            String secondWinOrTie = coefficients.get(5).text();
            Game game = new Game(leagueName, leagueLink, dateTime, firstTeam, secondTeam,
                    firstWin, tie, secondWin, firstWinOrTie, secondWinOrTie, GameResult.NO_RESULT, parsingTime);
            games.add(game);
        }
        return games;
    }

    private LocalDateTime processDate(Element gameElement) {
        String time = gameElement.select("div.c-events__time > span").text();
        String year = String.valueOf(LocalDate.now().getYear());
        time = time.replace(" ", String.format(".%s ", year)).substring(0, 16);
        return LocalDateTime.parse(time, FORMATTER);
    }
}
