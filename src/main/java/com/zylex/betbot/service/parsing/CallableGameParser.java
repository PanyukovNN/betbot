package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("WeakerAccess")
public class CallableGameParser implements Callable<List<Game>> {

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private DriverManager driverManager;

    private WebDriver driver;

    private WebDriverWait wait;

    private String league;

    public CallableGameParser(DriverManager driverManager, String league) {
        this.driverManager = driverManager;
        this.league = league;
    }

    /**
     * Processes parsing of one league and returns its games in list.
     * @return - list of games.
     */
    @Override
    public List<Game> call() {
        try {
            driver = driverManager.getDriver();
            wait = new WebDriverWait(driver, 2);
            return processGameParsing(driver);
        } finally {
            driverManager.addDriverToQueue(driver);
        }
    }

    private List<Game> processGameParsing(WebDriver driver) {
        driver.navigate().to(String.format("https://1xstavka.ru/%s", league));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Document document = Jsoup.parse(driver.getPageSource());
        List<Game> games = new ArrayList<>();
        String leagueName = document.select("a.c-events__liga").text();
        Elements gameElements = document.select("div.c-events__item_game");
        int nextDay = Calendar.getInstance().get(Calendar.DATE) + 1;
        for (Element gameElement : gameElements) {
            LocalDateTime dateTime = processDate(gameElement);
            if (nextDay > dateTime.getDayOfMonth()) {
                continue;
            } else if (nextDay < dateTime.getDayOfMonth()) {
                break;
            }
            Elements teams = gameElement.select("span.c-events__team");
            String firstTeam = teams.get(0).text();
            String secondTeam = teams.get(1).text();
            if (firstTeam.contains("Хозяева (голы)")) {
                continue;
            }
            Elements coefficients = gameElement.select("div.c-bets > a.c-bets__bet");
            String firstWin = coefficients.get(0).text();
            String tie = coefficients.get(1).text();
            String secondWin = coefficients.get(2).text();
            String firstWinOrTie = coefficients.get(3).text();
            String secondWinOrTie = coefficients.get(5).text();
            Game game = new Game(leagueName, dateTime, firstTeam, secondTeam, firstWin, tie, secondWin, firstWinOrTie, secondWinOrTie, league);
            games.add(game);
        }
        ConsoleLogger.logLeagueGame();
        return games;
    }

    private LocalDateTime processDate(Element gameElement) {
        String time = gameElement.select("div.c-events__time > span").text();
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        time = time.replace(" ", String.format(".%s ", year)).substring(0, 16);
        return LocalDateTime.parse(time, FORMATTER);
    }
}
