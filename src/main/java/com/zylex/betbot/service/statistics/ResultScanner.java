package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.logger.ResultScannerConsoleLogger;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.rule.RuleNumber;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Scans games results.
 */
public class ResultScanner {

    private ResultScannerConsoleLogger logger = new ResultScannerConsoleLogger();

    private WebDriver driver;

    private WebDriverWait wait;

    private GameDao gameDao;

    private int gamesToScan;

    public ResultScanner(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    /**
     * Finds games with no result, with links, and appropriate by time, then opens every game link,
     * pulls result of the game from site and saves this result to database.
     */
    public void process() {
        DriverManager driverManager = new DriverManager();

        try {
            Map<RuleNumber, List<Game>> ruleGames = findRuleGames();
            logger.startLogMessage(gamesToScan);
            if (ruleGames.isEmpty()) {
                logger.noGamesLog();
            } else {
                driverInit(driverManager);
            }
            processResults(ruleGames);
        } finally {
            driverManager.quitDriver();
        }
    }

    private Map<RuleNumber, List<Game>> findRuleGames() {
        Map<RuleNumber, List<Game>> ruleGames = new LinkedHashMap<>();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            List<Game> games = excludeGamesByTime(
                    findGamesWithLinks(
                            gameDao.getByRuleNumberWithNoResult(ruleNumber)
                    ));
            if (!games.isEmpty()) {
                ruleGames.put(ruleNumber, games);
            }
            gamesToScan += games.size();
        }
        return ruleGames;
    }

    private List<Game> findGamesWithLinks(List<Game> games) {
        return games.stream()
                .filter(game -> game.getLink() != null && !game.getLink().isEmpty())
                .collect(Collectors.toList());
    }

    private List<Game> excludeGamesByTime(List<Game> games) {
        return games.stream()
                .filter(game -> LocalDateTime.now().isAfter(game.getDateTime().plusHours(2)))
                .collect(Collectors.toList());
    }

    private void driverInit(DriverManager driverManager) {
        driver = driverManager.initiateDriver(false);
        wait = new WebDriverWait(driver, 5);
    }

    private void processResults(Map<RuleNumber, List<Game>> ruleGames) {
        for (RuleNumber ruleNumber : ruleGames.keySet()) {
            for (Game game : ruleGames.get(ruleNumber)) {
                driver.navigate().to("https://1xstavka.ru/" + game.getLink());
                driver.switchTo().frame(driver.findElement(By.className("statistic-after-game")));
                if (matchIsOver()) {
                    game.setGameResult(findGameResult());
                    gameDao.save(game, ruleNumber);
                    logger.logGame();
                }
            }
        }
    }

    private boolean matchIsOver() {
        try {
            return driver.findElement(By.className("match-info__text"))
                    .getText().contains("МАТЧ СОСТОЯЛСЯ");
        } catch (NoSuchElementException ignore) {
            return false;
        }
    }

    private GameResult findGameResult() {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.className("match-info__score")));
        String[] balls = driver.findElement(By.className("match-info__score")).getText().split(" : ");
        int firstBalls = Integer.parseInt(balls[0]);
        int secondBalls = Integer.parseInt(balls[1]);
        return computeGameResult(firstBalls, secondBalls);
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
