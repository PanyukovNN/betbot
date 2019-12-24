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

import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Scans games results.
 */
@SuppressWarnings("WeakerAccess")
public class ResultScanner {

    private ScannerConsoleLogger logger = new ScannerConsoleLogger();

    private WebDriver driver;

    private WebDriverWait wait;

    private GameDao gameDao;

    public ResultScanner(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    /**
     * Scans games results from site.
     * @param driverManager - instance of driver manager.
     */
    public List<Game> process(DriverManager driverManager, RuleNumber ruleNumber) {
        List<Game> games = findGamesWithLinks(gameDao.getByRuleNumberWithNoResult(ruleNumber));
        if (games.isEmpty()) {
            logger.endMessage(LogType.NO_GAMES_TO_SCAN);
            return Collections.emptyList();
        } else if (driver == null) {
            driverInit(driverManager);
        }
        logger.startLogMessage();

        processResults(ruleNumber, games);
        logger.endMessage(LogType.OK);
        return games;
    }

    private List<Game> findGamesWithLinks(List<Game> games) {
        return games.stream()
                .filter(game -> game.getLink() != null && !game.getLink().isEmpty())
                .collect(Collectors.toList());
    }

    private void driverInit(DriverManager driverManager) {
        driver = driverManager.initiateDriver(false);
        wait = new WebDriverWait(driver, 20);
    }

    private void processResults(RuleNumber ruleNumber, List<Game> games) {
        for (Game game : games) {
            driver.navigate().to("https://1xstavka.ru/" + game.getLink());
            driver.switchTo().frame(driver.findElement(By.className("statistic-after-game")));
            if (matchOver()) {
                findGameResult(game);
                gameDao.save(game, ruleNumber);
            }
        }
    }

    private boolean matchOver() {
        try {
            return driver.findElement(By.className("match-info__text"))
                    .getText().contains("Матч состоялся");
        } catch (NoSuchElementException ignore) {
            return false;
        }
    }

    private void findGameResult(Game game) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.className("match-info__score")));
        String[] balls = driver.findElement(By.className("match-info__score")).getText().split(" : ");
        int firstBalls = Integer.parseInt(balls[0]);
        int secondBalls = Integer.parseInt(balls[1]);
        GameResult gameResult = computeGameResult(firstBalls, secondBalls);
        game.setGameResult(gameResult);
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
