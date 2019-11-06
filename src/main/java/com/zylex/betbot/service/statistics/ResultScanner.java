package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.ResultScannerConsoleLogger;
import com.zylex.betbot.exception.ResultsScannerException;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ResultScanner {

    private ResultScannerConsoleLogger logger = new ResultScannerConsoleLogger();

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private WebDriver driver;

    private WebDriverWait wait;

    private Set<Integer> days = new HashSet<>();

    private File betMadeFile = new File(String.format("results/%s/BET_MADE_%s.csv", LocalDate.now().getMonth().name(), LocalDate.now().getMonth().name()));

    public void process() {
        try {
            initiateDriver();
            logger.startLogMessage();
            openFootballGamesResults();
            List<Game> betMadeGames = readBetMadeGames();
            processGameResults(betMadeGames);
            saveBetsMadeGamesToFile(betMadeGames);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private List<Game> readBetMadeGames() throws IOException {
        List<Game> betMadeGames = new ArrayList<>();
        if (!betMadeFile.exists()) {
            betMadeFile.createNewFile();
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
            RuleNumber ruleNumber = RuleNumber.valueOf(fields[6]);
            GameResult gameResult = GameResult.NO_RESULT;
            if (fields.length > 7) {
                gameResult = GameResult.valueOf(fields[7]);
            }
            Game game = new Game(league, leagueLink, dateTime, firstTeam, secondTeam, ruleNumber, gameResult);
            betMadeGames.add(game);
            days.add(dateTime.getDayOfMonth());
        }
        return betMadeGames;
    }

    private void saveBetsMadeGamesToFile(List<Game> madeBetsGames) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(betMadeFile), StandardCharsets.UTF_8))) {
            String MADE_BET_GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s\n";
            for (Game game : madeBetsGames) {
                String line = String.format(MADE_BET_GAME_FORMAT,
                        game.getLeague(),
                        game.getLeagueLink(),
                        DATE_FORMATTER.format(game.getDateTime()),
                        game.getFirstTeam(),
                        game.getSecondTeam(),
                        game.getRuleNumber(),
                        game.getGameResult());
                writer.write(line);
            }
        }
    }

    private void processGameResults(List<Game> betsMadeGames) {
        for (int day : days) {
            navigateToDay(day);
            waitElementsAndGet("c-games__row");
            Document document = Jsoup.parse(driver.getPageSource());
            Elements gameElements = document.select("div[class=c-games__row u-nvpd c-games__row_light c-games__row_can-toggle]");
            List<Game> betsMadeNoResultGames = findNoResultGames(betsMadeGames, day);
            parseGameResults(gameElements, betsMadeNoResultGames);
        }
    }

    private void parseGameResults(Elements gameElements, List<Game> betsMadeNoResultGames) {
        AtomicInteger i = new AtomicInteger();
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
                        logger.logBetMadeGame(i.incrementAndGet(), game);
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
    private void navigateToDay(int day) {
        waitSingleElementAndGet("vdp-datepicker").click();
        waitElementsAndGet("day").stream()
                .filter(element -> element.getText().equals(String.valueOf(day)))
                .findFirst()
                .get()
                .click();
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
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElements(By.className(className));
    }

    private WebElement waitSingleElementAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElement(By.className(className));
    }
}
