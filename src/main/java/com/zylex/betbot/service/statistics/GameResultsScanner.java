package com.zylex.betbot.service.statistics;

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
import java.util.stream.Collectors;

public class GameResultsScanner {

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private static WebDriver driver;

    private static WebDriverWait wait;

    private static List<Game> betsMadeGames = new ArrayList<>();

    private static Set<Integer> days = new HashSet<>();

    public static void main(String[] args) {
        try {
            DriverManager driverManager = new DriverManager();
            driverManager.initiateDriver(false);
            driver = driverManager.getDriver();
            wait = new WebDriverWait(driver, 10);
            driver.navigate().to("https://1xstavka.ru/results/");
            System.out.println();
            openAllGamesResults();

            readBetsMadeGames();

            int currentDay = LocalDate.now().getDayOfMonth();
            parseElements(currentDay);

            int previousDay = LocalDate.now().minusDays(1).getDayOfMonth();
            navigateToDay(previousDay);
            parseElements(previousDay);

            saveBetsMadeGamesToFile(betsMadeGames);



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void navigateToDay(int day) {
        waitSingleElementAndGet("vdp-datepicker").click();
        waitElementsAndGet("day").stream()
                .filter(element -> element.getText().equals(String.valueOf(day)))
                .findFirst()
                .get()
                .click();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void openAllGamesResults() {
        waitElementsAndGet("c-nav__link").get(1).click();
        waitSingleElementAndGet("c-filter_filled").click();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void readBetsMadeGames() throws IOException {
        File file = new File(String.format("results/%s/BETS_MADE_%s.csv", "NOVEMBER", "NOVEMBER"));
        if (!file.exists()) {
            file.createNewFile();
            return;
        }
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            String[] fields = line.split(";");
            Game game;
            if (fields.length == 7) {
                game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], RuleNumber.valueOf(fields[6]), GameResult.NO_RESULT);
            } else {
                game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                        fields[4], fields[5], RuleNumber.valueOf(fields[6]), GameResult.valueOf(fields[6]));
            }
            betsMadeGames.add(game);
        }
    }

    private static void saveBetsMadeGamesToFile(List<Game> madeBetsGames) throws IOException {
        File file = new File(String.format("results/%s/BETS_MADE_%s.csv", "NOVEMBER", "NOVEMBER"));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
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

    private static void parseElements(int day) {
        waitElementsAndGet("c-games__row");
        Document document = Jsoup.parse(driver.getPageSource());
        Elements elements = document.select("div[class=c-games__row u-nvpd c-games__row_light c-games__row_can-toggle]");
        List<Game> betsMadeNoResultGames = betsMadeGames.stream()
                .filter(game -> game.getGameResult() == GameResult.NO_RESULT && game.getDateTime().getDayOfMonth() == day)
                .collect(Collectors.toList());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Element element : elements) {
            String dateText = element.select("div.c-games__date").text();
            dateText = dateText.substring(0, 5) + "." + LocalDateTime.now().getYear() + dateText.substring(5);
            LocalDateTime date = LocalDateTime.parse(dateText, dateTimeFormatter);

            String[] teams = element.select("div[class=c-games__opponents u-dir-ltr]").text().split(" - ");
            if (teams.length < 2) {
                continue;
            }
            String firstTeam = teams[0];
            String secondTeam = teams[1];
            if (firstTeam.contains("(голы)") || firstTeam.contains("/") || firstTeam.contains("(люб)")) {
                continue;
            }

            String[] balls = element.select("div[class=c-games__results u-mla u-tar]").text()
                    .split("\\(")[0]
                    .trim()
                    .split(":");
            if (balls[0].contains("Голы")) {
                continue;
            }
            int firstBalls = Integer.parseInt(balls[0]);
            int secondBalls = Integer.parseInt(balls[1]);
            GameResult gameResult = computeGameResult(firstBalls, secondBalls);


            Optional<Game> betMadeNoResultGameOptional = betsMadeNoResultGames.stream().filter(
                    game -> game.getDateTime().toLocalDate().equals(date.toLocalDate())
                            && firstTeam.startsWith(game.getFirstTeam())
                            && secondTeam.startsWith(game.getSecondTeam()))
                    .findFirst();

            if (betMadeNoResultGameOptional.isPresent()) {
                Game betMadeNoResultGame = betMadeNoResultGameOptional.get();
                betMadeNoResultGame.setGameResult(gameResult);
            }
        }

        betsMadeNoResultGames.forEach(System.out::println);
    }

    private static GameResult computeGameResult(int firstBalls, int secondBalls) {
        if (firstBalls > secondBalls) {
            return GameResult.FIRST_WIN;
        } else if (firstBalls == secondBalls) {
            return GameResult.TIE;
        } else {
            return GameResult.SECOND_WIN;
        }
    }

    private static List<WebElement> waitElementsAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElements(By.className(className));
    }

    private static WebElement waitSingleElementAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElement(By.className(className));
    }
}
