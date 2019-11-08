package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.BetRepository;
import com.zylex.betbot.controller.logger.ResultScannerConsoleLogger;
import com.zylex.betbot.exception.ResultsScannerException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ResultScanner {

    private ResultScannerConsoleLogger logger = new ResultScannerConsoleLogger();


    private WebDriver driver;

    private WebDriverWait wait;

    private Set<LocalDate> days = new HashSet<>();

    private BetRepository betRepository;

    public ResultScanner(BetRepository betRepository) {
        this.betRepository = betRepository;
    }

    public void process() {
        try {
            initiateDriver();
            logger.startLogMessage();
            openFootballGamesResults();
            List<Game> betMadeGames = betRepository.readTotalBetMadeFile();
            betMadeGames.forEach(game -> days.add(game.getDateTime().toLocalDate()));
            processGameResults(betMadeGames);
            betRepository.saveTotalBetGamesToFile(betMadeGames);
            logger.endMessage();
        } catch (IOException e) {
            throw new ResultsScannerException(e.getMessage(), e);
        } finally {
            driver.quit();
        }
    }

    private void initiateDriver() {
        DriverManager driverManager = new DriverManager();
        driverManager.initiateDriver(true);
        driver = driverManager.getDriver();
        wait = new WebDriverWait(driver, 10);
        driver.navigate().to("https://1xstavka.ru/results/");
    }

    private void openFootballGamesResults() {
        waitElementsAndGet("c-nav__link").get(1).click();
        waitSingleElementAndGet("c-filter_filled").click();
    }

    private void processGameResults(List<Game> betsMadeGames) {
        for (LocalDate day : days) {
            navigateToDay(day);
            waitElementsAndGet("c-games__row");
            Document document = Jsoup.parse(driver.getPageSource());
            Elements gameElements = document.select("div[class=c-games__row u-nvpd c-games__row_light c-games__row_can-toggle]");
            List<Game> betsMadeNoResultGames = findNoResultGames(betsMadeGames, day.getDayOfMonth());
            parseGameResults(gameElements, betsMadeNoResultGames);
        }
    }

    private void parseGameResults(Elements gameElements, List<Game> betsMadeNoResultGames) {
        for (Element gameElement : gameElements) {
            LocalDateTime date = processDateTime(gameElement);
            String[] teams = gameElement.select("div[class=c-games__opponents u-dir-ltr]").text().split(" - ");
            if (teams.length < 2 || teams[0].contains("(голы)") || teams[0].contains("/") || teams[0].contains("(люб)")) {
                continue;
            }
            String firstTeam = teams[0];
            String secondTeam = teams[1];
            String[] balls = gameElement.select("div[class=c-games__results u-mla u-tar]").text()
                    .replace(" ", "")
                    .split("\\(")[0]
                    .split(":");
            if (balls[0].contains("Голы")) {
                continue;
            }
            int firstBalls = Integer.parseInt(balls[0]);
            int secondBalls = Integer.parseInt(balls[1]);
            GameResult gameResult = computeGameResult(firstBalls, secondBalls);
            betsMadeNoResultGames.stream().filter(
                    game -> game.getDateTime().toLocalDate().equals(date.toLocalDate())
                            && firstTeam.startsWith(game.getFirstTeam())
                            && secondTeam.startsWith(game.getSecondTeam()))
                    .findFirst()
                    .ifPresent(game -> {
                        game.setGameResult(gameResult);
                        logger.logBetMadeGame(game);
                    });
        }
    }

    private LocalDateTime processDateTime(Element element) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String dateText = element.select("div.c-games__date").text();
        dateText = dateText.substring(0, 5) + "." + LocalDateTime.now().getYear() + dateText.substring(5);
        return LocalDateTime.parse(dateText, dateTimeFormatter);
    }

    private List<Game> findNoResultGames(List<Game> betsMadeGames, int day) {
        return betsMadeGames.stream()
                .filter(game -> game.getGameResult() == GameResult.NO_RESULT
                        && game.getDateTime().getDayOfMonth() == day)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void navigateToDay(LocalDate day) {
        if (day.getMonth().equals(LocalDate.now().getMonth())) {
            waitSingleElementAndGet("vdp-datepicker").click();
            WebElement nextButton = waitSingleElementAndGet("next");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", nextButton);
            waitElementsAndGet("day").stream()
                    .filter(element -> element.getText().equals(String.valueOf(day.getDayOfMonth())))
                    .findFirst()
                    .get()
                    .click();
        } else if (day.getMonth().equals(LocalDate.now().minusMonths(1).getMonth())) {
            waitSingleElementAndGet("vdp-datepicker").click();
            waitSingleElementAndGet("prev").click();
            waitElementsAndGet("day").stream()
                    .filter(element -> element.getText().equals(String.valueOf(day.getDayOfMonth())))
                    .findFirst()
                    .get()
                    .click();
        }
    }

    private GameResult computeGameResult(int firstBalls, int secondBalls) {
        if (firstBalls > secondBalls) {
            return GameResult.FIRST_WIN;
        } else if (firstBalls == secondBalls) {
            return GameResult.TIE;
        } else {
            return GameResult.SECOND_WIN;
        }
    }

    private List<WebElement> waitElementsAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
        return driver.findElements(By.className(className));
    }

    private WebElement waitSingleElementAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
        return driver.findElement(By.className(className));
    }
}
