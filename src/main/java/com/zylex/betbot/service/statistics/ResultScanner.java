package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ResultScannerConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.driver.DriverManager;
import com.zylex.betbot.service.repository.GameRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.zylex.betbot.BetBotApplication.botStartTime;

/**
 * Scans games results for last 3 days.
 */
@Service
public class ResultScanner {

    private ResultScannerConsoleLogger logger = new ResultScannerConsoleLogger();

    private GameRepository gameRepository;

    private DriverManager driverManager;

    private LocalDate currentDay;

    private int gamesToScan;

    @Autowired
    public ResultScanner(GameRepository gameRepository,
                         DriverManager driverManager) {
        this.gameRepository = gameRepository;
        this.driverManager = driverManager;
    }

    @Transactional
    public void scan() {
        try {
//            List<Game> todayGames = gameRepository.getByDate(LocalDate.now());
//            todayGames.forEach(System.out::println);
            List<Game> noResultGames = findNoResultGames();
            logger.startLogMessage(LogType.PARSING_SITE_START, 0);
            if (noResultGames.isEmpty()) {
                logger.noGamesLog();
            } else {
                driverInit(driverManager);
                logger.startLogMessage(LogType.GAMES, gamesToScan);
            }
            processResults(noResultGames);
        } finally {
            driverManager.quitDriver();
        }
    }

    private List<Game> findNoResultGames() {
        List<Game> noResultGames = gameRepository
                .getSinceDateTime(LocalDateTime.of(botStartTime.toLocalDate().minusDays(2), LocalTime.MIN)).stream()
                .filter(game -> botStartTime.isAfter(game.getDateTime().plusHours(2)))
                .filter(game -> game.getResult().equals(GameResult.NO_RESULT.toString()))
                .sorted(Comparator.comparing(Game::getDateTime))
                .collect(Collectors.toList());
        gamesToScan = noResultGames.size();
        return noResultGames;
    }

    private void driverInit(DriverManager driverManager) {
        if (driverManager.getDriver() == null) {
            driverManager.initiateDriver(false);
        }
    }

    private void processResults(List<Game> noResultGames) {
        driverManager.getDriver().get("https://1xstavka.ru/results/");
        WebElement footballTab = driverManager.getDriver()
                .findElement(By.className("ps-container"))
                .findElement(By.className("c-nav"))
                .findElements(By.className("c-nav__item")).get(1)
                .findElement(By.cssSelector("a"));
        System.out.println(footballTab.getText());
        if (footballTab.getAttribute("title").equals("Футбол")) {
            footballTab.click();
        } else {
            System.out.println("Problem");
            return;
        }
        driverManager.getDriver().findElements(By.className("u-no-grow_md")).get(1).click();  // Развернуть все
        for (LocalDate day = LocalDate.now();
             day.isAfter(LocalDate.now().minusDays(3));
             day = day.minusDays(1)) {
            Document document = Jsoup.parse(driverManager.getDriver().getPageSource());
            currentDay = day;
            if (!day.isEqual(LocalDate.now())) {
                navigateToDay(day);
            }
            List<Game> dayGames = noResultGames.stream()
                    .filter(game -> game.getDateTime().toLocalDate().isEqual(currentDay))
                    .collect(Collectors.toList());
            Elements gameElements = document.select("div.c-table__row");
            for (Element gameElement : gameElements) {
                Element teamCellElement = gameElement.selectFirst("div.c-games__opponents");
                if (teamCellElement == null) continue;
                String teamCellText = teamCellElement.text();
                if (!teamCellText.contains(" - ")) continue;
                String[] teams = teamCellText.split(" - ", 2);
                String firstTeam = teams[0];
                String secondTeam = teams[1];
                for (Game game : dayGames) {
                    if (!game.getFirstTeam().equals(firstTeam)) continue;
                    if (!game.getSecondTeam().equals(secondTeam)) continue;
                    String scoreCellText = gameElement.selectFirst("div.c-games__results").text();
                    if (!scoreCellText.contains(" (")) continue;
                    String[] scores = scoreCellText.split(" \\(", 2)[0].split(":");
                    int firstBalls = Integer.parseInt(scores[0]);
                    int secondBalls = Integer.parseInt(scores[1]);
                    GameResult gameResult = computeGameResult(firstBalls, secondBalls);
                    game.setResult(gameResult.toString());
                    gameRepository.update(game);
                    System.out.println(game);
                    logger.logGame();
                    dayGames.remove(game);
                    break;
                }
            }
        }
    }

    private void navigateToDay(LocalDate day) {
        WebElement datepickerElement = driverManager.getDriver()
                .findElement(By.className("vdp-datepicker"))
                .findElement(By.cssSelector("input"));
        datepickerElement.click();
        if (currentDay.getMonth().equals(LocalDate.now().minusMonths(1).getMonth())) {
            driverManager.waitElement(By::className, "vdp-datepicker__calendar")
                    .findElement(By.cssSelector("header"))
                    .findElement(By.className("prev"))
                    .click();
        }
        List<WebElement> dayElements = driverManager.waitElement(By::className, "vdp-datepicker__calendar")
                .findElements(By.cssSelector("span"));
        for (WebElement dayElement : dayElements) {
            if (dayElement.getText().equals(String.valueOf(day.getDayOfMonth()))) {
                dayElement.click();
            }
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
}
