package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import com.zylex.betbot.service.bet.rule.RuleProcessor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Making bets.
 */
public class BetProcessor {

    private BetConsoleLogger logger = new BetConsoleLogger();

    private WebDriver driver;

    private WebDriverWait wait;

    private RuleNumber ruleNumber;

    private RuleProcessor ruleProcessor;

    private Repository repository;

    private boolean mock;

    public BetProcessor(RuleProcessor ruleProcessor, RuleNumber ruleNumber, boolean mock) {
        this.ruleProcessor = ruleProcessor;
        this.ruleNumber = ruleNumber;
        this.mock = mock;
        this.repository = ruleProcessor.getRepository();
    }

    /**
     * Initiates one non-headless chrome driver, navigates to site,
     * logs in and makes bets.
     */
    public void process() {
        List<Game> betGames = findAppropriateGames(ruleProcessor.process());
        try {
            if (betGames.isEmpty()) {
                logger.betMade(LogType.ERROR);
                return;
            }
            openSite();
            List<Game> betMadeGames = processBets(betGames);
            if (!mock) {
                repository.saveTotalBetMadeGamesToFile(betMadeGames);
            }
            logger.betMade(LogType.OK);
        } catch (IOException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private List<Game> findAppropriateGames(List<Game> betGames) {
        return filterByBetMade(filterByParsingTime(betGames));
    }

    private List<Game> filterByBetMade(List<Game> filteredBetGames) {
        List<Game> betMadeGames = repository.readTotalBetMadeFile();
        return filteredBetGames.stream().filter(game -> !betMadeGames.contains(game)).collect(Collectors.toList());
    }

    private List<Game> filterByParsingTime(List<Game> betGames) {
        return betGames.stream()
                .filter(game -> game.getParsingTime().isAfter(LocalDateTime.of(game.getDateTime().toLocalDate().minusDays(1), LocalTime.of(22,59))))
                .collect(Collectors.toList());
    }

    private void openSite() throws IOException {
        driverInit();
        logger.logRule(ruleNumber);
        logger.startLogMessage(LogType.LOG_IN);
        logIn();
        logger.startLogMessage(LogType.BET);
    }

    private void driverInit() {
        if (driver == null) {
            DriverManager driverManager = new DriverManager();
            driver = driverManager.initiateDriver(false);
            wait = new WebDriverWait(driver, 5);
            driver.navigate().to("https://1xstavka.ru/");
        }
    }

    private void logIn() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("oneXBetAuth.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            waitSingleElementAndGet("base_auth_form").click();
            List<WebElement> authenticationForm = waitElementsAndGet("c-input-material__input");
            authenticationForm.get(0).sendKeys(property.getProperty("oneXBet.login"));
            authenticationForm.get(1).sendKeys(property.getProperty("oneXBet.password"));
            waitSingleElementAndGet("auth-button").click();
            checkVerify();
        }
    }

    private void checkVerify() {
        try {
            Thread.sleep(1500);
            String url = driver.getCurrentUrl();
            if (url.contains("accountverify")) {
                logger.logInLog(LogType.VERIFY);
            } else {
                logger.logInLog(LogType.OK);
            }
        } catch (InterruptedException e) {
            throw new BetProcessorException(e.getMessage(), e);
        }
    }

    private List<Game> processBets(List<Game> betGames) {
        BetCoefficient betCoefficient = ruleNumber.betCoefficient;
        double totalMoney = Double.parseDouble(waitSingleElementAndGet("top-b-acc__amount").getText());
        int singleBetAmount = calculateAmount(totalMoney);
        double availableBalance = totalMoney;
        List<Game> betMadeGames = new ArrayList<>();
        int i = 0;
        for (Game game : betGames) {
            if (availableBalance < singleBetAmount) {
                logger.noMoney();
                break;
            }
            List<WebElement> coefficients = getGameCoefficients(game);
            if (coefficients.size() > 0) {
                coefficients.get(betCoefficient.INDEX).click();
                if (makeBet(singleBetAmount, mock)) {
                    availableBalance -= singleBetAmount;
                    betMadeGames.add(game);
                    logger.logBet(++i, singleBetAmount, betCoefficient, game, LogType.OK);
                }
            }
        }
        return betMadeGames;
    }

    private int calculateAmount(double totalMoney) {
        double singleBetMoney = totalMoney * ruleNumber.PERCENT;
        return (int) Math.max(singleBetMoney, 20);
    }

    private boolean makeBet(double amount, boolean mock) {
        driver.findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        if (!mock) {
            WebElement betButton = waitSingleElementAndGet("coupon-btn-group__item")
                    .findElement(By.cssSelector("button"));
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", betButton);
            return okButtonClick(executor);
        }
        return true;
    }

    private boolean okButtonClick(JavascriptExecutor executor) {
        try {
            WebElement okButton = waitSingleElementAndGet("o-btn-group__item")
                    .findElement(By.cssSelector("button"));
            executor.executeScript("arguments[0].click();", okButton);
        } catch (TimeoutException e) {
            WebElement okButton = waitSingleElementAndGet("swal2-confirm");
            executor.executeScript("arguments[0].click();", okButton);
            WebElement delButton = waitSingleElementAndGet("c-bet-box__del");
            executor.executeScript("arguments[0].click();", delButton);
            logger.logBet(0, 0, null, null, LogType.BET_ERROR);
            return false;
        }
        return true;
    }

    private List<WebElement> getGameCoefficients(Game game) {
        driver.navigate().to("https://1xstavka.ru/line/Football/" + game.getLeagueLink());
        List<WebElement> gameWebElements = waitElementsAndGet("c-events__item_game");
        for (WebElement gameWebElement : gameWebElements) {
            LocalDateTime dateTime = processDateTime(gameWebElement);
            if (!dateTime.equals(game.getDateTime())) {
                continue;
            }
            List<WebElement> teams = gameWebElement.findElements(By.className("c-events__team"));
            String firstTeam = teams.get(0).getText();
            String secondTeam = teams.get(1).getText();
            if (firstTeam.equals(game.getFirstTeam())
                    && secondTeam.equals(game.getSecondTeam())) {
                return gameWebElement.findElements(By.className("c-bets__bet"));
            }
        }
        logger.logBet(0, 0, null, game, LogType.BET_NOT_FOUND);
        return new ArrayList<>();
    }

    private LocalDateTime processDateTime(WebElement gameElement) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String dateTime = gameElement.findElement(By.className("c-events__time"))
                .findElement(By.cssSelector("span"))
                .getText();
        String year = String.valueOf(LocalDate.now().getYear());
        dateTime = dateTime.replace(" ", String.format(".%s ", year)).substring(0, 16);
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
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
