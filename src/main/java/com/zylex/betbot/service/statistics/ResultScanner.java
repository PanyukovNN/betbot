package com.zylex.betbot.service.statistics;

import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.controller.logger.ResultScannerConsoleLogger;
import com.zylex.betbot.exception.ResultScannerException;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.game.GameResult;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.zylex.betbot.BetBotApplication.botStartTime;

/**
 * Scans games results since specified date.
 */
@Service
public class ResultScanner {

    private ResultScannerConsoleLogger logger = new ResultScannerConsoleLogger();

    private GameRepository gameRepository;

    private DriverManager driverManager;

    @Autowired
    public ResultScanner(GameRepository gameRepository,
                         DriverManager driverManager) {
        this.gameRepository = gameRepository;
        this.driverManager = driverManager;
    }

    /**
     * Navigates to results tab, where opens specified day statistics
     * and parse games results.
     * @param startDate - start date for scanning.
     */
    @Transactional
    public void scan(LocalDate startDate) {
        List<Game> noResultGames = findNoResultGames(startDate);
        logger.startLogMessage(LogType.PARSING_SITE_START, 0);
        if (noResultGames.isEmpty()) {
            logger.noGamesLog();
        } else {
            if (driverManager.getDriver() == null) {
                driverManager.initiateDriver(false);
            }
            logger.startLogMessage(LogType.GAMES, noResultGames.size());
            processResults(noResultGames, startDate);
        }
    }

    private List<Game> findNoResultGames(LocalDate startDate) {
        return gameRepository
                .getSinceDateTime(LocalDateTime.of(startDate, LocalTime.MIN)).stream()
                .filter(game -> botStartTime.isAfter(game.getDateTime().plusHours(2)))
                .filter(game -> game.getResult().equals(GameResult.NO_RESULT.toString()))
                .sorted(Comparator.comparing(Game::getDateTime))
                .collect(Collectors.toList());
    }

    private void processResults(List<Game> noResultGames, LocalDate startDate) {
        driverManager.getDriver().get("https://1xstavka.ru/results/");
        clickFootballTab();
        expandAll();
        for (LocalDate day = LocalDate.now();
             day.isAfter(startDate.minusDays(1));
             day = day.minusDays(1)) {
            List<Game> dayGames = findDayGames(noResultGames, day);
            if (dayGames.isEmpty()) continue;
            navigateToDay(day);
            Document document = Jsoup.parse(driverManager.getDriver().getPageSource());
            Elements gameElements = document.select("div.c-table__row");
            for (Element gameElement : gameElements) {
                String[] teams = findTeams(gameElement);
                if (teams.length == 0) continue;
                for (Game game : dayGames) {
                    if (!game.getFirstTeam().equals(teams[0])) continue;
                    if (!game.getSecondTeam().equals(teams[1])) continue;
                    processGame(gameElement, game);
                    dayGames.remove(game);
                    break;
                }
            }
            for (Game game : dayGames) {
                if (game.getDateTime().isBefore(botStartTime.minusDays(1))) {
                    game.setResult(GameResult.NOT_FOUND.toString());
                    gameRepository.update(game);
                }
            }
        }
    }

    private void clickFootballTab() {
        WebElement footballTab = driverManager.waitElements(By::className, "c-nav__link").get(1);
        if (footballTab.getAttribute("title").equals("Футбол")) {
            footballTab.click();
        } else {
            throw new ResultScannerException("No football tab.");
        }
    }

    private void expandAll() {
        driverManager.getDriver()
                .findElements(By.className("u-no-grow_md"))
                .get(1)
                .click();
    }

    private void navigateToDay(LocalDate day) {
        if (day.isEqual(LocalDate.now())) return;
        WebElement datepickerElement = driverManager.getDriver()
                .findElement(By.className("vdp-datepicker"))
                .findElement(By.cssSelector("input"));
        datepickerElement.click();
        navigateToPreviousMonth(day);
        List<WebElement> dayElements = driverManager.waitElement(By::className, "vdp-datepicker__calendar")
                .findElements(By.cssSelector("span"));
        for (WebElement dayElement : dayElements) {
            if (dayElement.getText().equals(String.valueOf(day.getDayOfMonth()))) {
                dayElement.click();
            }
        }
        driverManager.waitElement(By::className, "c-table__row");
    }

    private void navigateToPreviousMonth(LocalDate day) {
        WebElement datepickerHeaderElement = driverManager.waitElement(By::className, "vdp-datepicker__calendar")
                .findElement(By.cssSelector("header"));
        String monthName = datepickerHeaderElement.findElement(By.className("day__month_btn")).getText().split(" ")[0];
        Month currentMonth = LocalDate.now().getMonth();
        String currentMonthName = currentMonth.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"));
        if (monthName.equalsIgnoreCase(currentMonthName)) {
            Month previousMonth = currentMonth.minus(1);
            String previousMonthName = previousMonth.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru"));
            if (day.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru")).equalsIgnoreCase(previousMonthName)) {
                datepickerHeaderElement
                        .findElement(By.className("prev"))
                        .click();
            }
        }
    }

    private List<Game> findDayGames(List<Game> noResultGames, LocalDate day) {
        return noResultGames.stream()
                .filter(game -> game.getDateTime().toLocalDate().isEqual(day))
                .collect(Collectors.toList());
    }

    private String[] findTeams(Element gameElement) {
        Element teamCellElement = gameElement.selectFirst("div.c-games__opponents");
        if (teamCellElement == null) return new String[]{};
        String teamCellText = teamCellElement.text();
        if (!teamCellText.contains(" - ")) return new String[]{};
        return teamCellText.split(" - ", 2);
    }

    private void processGame(Element gameElement, Game game) {
        String scoreCellText = gameElement.selectFirst("div.c-games__results").text();
        if (!scoreCellText.contains(" (")) return;
        String[] scores = scoreCellText.split(" \\(", 2)[0].split(":");
        int firstBalls = Integer.parseInt(scores[0]);
        int secondBalls = Integer.parseInt(scores[1]);
        GameResult gameResult = computeGameResult(firstBalls, secondBalls);
        game.setResult(gameResult.toString());
        gameRepository.update(game);
        logger.logGame();
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
