package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ScannerConsoleLogger;
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

    private ScannerConsoleLogger logger = new ScannerConsoleLogger();

    private WebDriver driver;

    private WebDriverWait wait;

    private GameDao gameDao;

    public ResultScanner(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    /**
     * Finds games with no result, with links, and appropriate by time, then opens every game link,
     * pulls result of the game from site and saves this result to database.
     */
    public void process() {
        DriverManager driverManager = new DriverManager();
        for (RuleNumber ruleNumber : RuleNumber.values()) {
            List<Game> games = excludeGamesByTime(
                    findGamesWithLinks(
                            gameDao.getByRuleNumberWithNoResult(ruleNumber)
                    ));
            if (games.isEmpty()) {
                logger.endMessage(LogType.NO_GAMES_TO_SCAN);
            } else if (driver == null) {
                driverInit(driverManager);
            }
            logger.startLogMessage();
            processResults(ruleNumber, games);
            logger.endMessage(LogType.OK);
        }
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

    private void processResults(RuleNumber ruleNumber, List<Game> games) {
        for (Game game : games) {
            driver.navigate().to("https://1xstavka.ru/" + game.getLink());
            driver.switchTo().frame(driver.findElement(By.className("statistic-after-game")));
            boolean what = matchIsOver();
            if (what) {
                game.setGameResult(findGameResult());
                gameDao.save(game, ruleNumber);
            }
        }
    }

    private boolean matchIsOver() {
        try {
            return driver.findElement(By.className("match-info__text"))
                    .getText().contains("МАТЧ СОСТОЯЛСЯ");
        } catch (NoSuchElementException ignore) {
            System.out.println("Exception");
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
