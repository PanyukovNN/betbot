package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameInfo;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.model.League;
import com.zylex.betbot.service.Day;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Thread for parsing one league link.
 */
public class CallableGameParser implements Callable<List<Game>> {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private String leagueLink;

    public CallableGameParser(String leagueLink) {
        this.leagueLink = leagueLink;
    }

    /**
     * Parsing by jsoup league link on the site and return all matches for today and tomorrow.
     * @return - list of games.
     */
    @Override
    @Transactional
    public List<Game> call() {
        try {
            return processGameParsing();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private List<Game> processGameParsing() throws IOException {
        ParsingConsoleLogger.logLeagueGame();
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
            double firstWin = stringToDouble(coefficients.get(0).text());
            double tie = stringToDouble(coefficients.get(1).text());
            double secondWin = stringToDouble(coefficients.get(2).text());
            double firstWinOrTie = stringToDouble(coefficients.get(3).text());
            double secondWinOrTie = stringToDouble(coefficients.get(5).text());
            String link = gameElement.select("a.c-events__name")
                    .attr("href")
                    .replaceFirst("line", "live");
            League league = new League(leagueName, leagueLink);
            GameInfo gameInfo = new GameInfo(firstWin, tie, secondWin, firstWinOrTie, secondWinOrTie);
            Game game = new Game(dateTime, league, firstTeam, secondTeam, GameResult.NO_RESULT.toString(), false, link, gameInfo);
            gameInfo.setGame(game);
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

    private double stringToDouble(String value) {
        if (value.equals("-") || value.isEmpty()) {
            return 0d;
        } else {
            return Double.parseDouble(value);
        }
    }
}
