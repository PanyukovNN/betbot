package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.repository.BalanceRepository;
import com.zylex.betbot.controller.repository.GameRepository;
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

    private List<RuleNumber> ruleList;

    private RuleProcessor ruleProcessor;

    private GameRepository gameRepository;

    private BalanceRepository balanceRepository;

    private int totalBalance = -1;

    private int availableBalance = -1;

    public BetProcessor(RuleProcessor ruleProcessor, BalanceRepository balanceRepository, List<RuleNumber> ruleList) {
        this.ruleProcessor = ruleProcessor;
        this.balanceRepository = balanceRepository;
        this.gameRepository = ruleProcessor.getGameRepository();
        this.ruleList = ruleList;
    }

    /**
     * Initiates non-headless chrome driver, opens the site, logs in,
     * makes bets, and saves bet made games to files.
     */
    public void process() {
        Map<RuleNumber, List<Game>> ruleGames = ruleProcessor.process();
        if (ruleList.isEmpty()) {
            return;
        }
        try {
            for (RuleNumber ruleNumber : ruleList) {
                List<Game> games = ruleGames.get(ruleNumber);
                List<Game> betGames = findBetGames(games);
                if (betGames.isEmpty()) {
                    continue;
                }
                openSite();
                List<Game> betMadeGames = processBets(ruleNumber, betGames);
                gameRepository.appendSaveByRule(ruleNumber, betMadeGames);
            }
            if (driver == null) {
                logger.betMade(LogType.NO_GAMES_TO_BET);
            } else {
                logger.betMade(LogType.OK);
            }
        } catch (IOException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private List<Game> findBetGames(List<Game> betGames) {
        return filterByBetMade(filterByParsingTime(betGames));
    }

    private List<Game> filterByBetMade(List<Game> filteredBetGames) {
        return filteredBetGames.stream().filter(game -> game.getBetMade() <= 0).collect(Collectors.toList());
    }

    private List<Game> filterByParsingTime(List<Game> betGames) {
        return betGames.stream()
                .filter(game -> LocalDateTime.now().isAfter(LocalDateTime.of(game.getDateTime().toLocalDate().minusDays(1), LocalTime.of(22,59))))
                .collect(Collectors.toList());
    }

    private void openSite() throws IOException {
        if (driver == null) {
            DriverManager driverManager = new DriverManager();
            driver = driverManager.initiateDriver(false);
            wait = new WebDriverWait(driver, 5);
            driver.navigate().to("https://1xstavka.ru/");
            logger.startLogMessage(LogType.LOG_IN, "");
            logIn();
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
            //TODO
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

    private List<Game> processBets(RuleNumber ruleNumber, List<Game> betGames) {
        logger.startLogMessage(LogType.BET, ruleNumber.toString());
        BetCoefficient betCoefficient = ruleNumber.betCoefficient;
        updateBalance();
        int singleBetAmount = calculateAmount(ruleNumber);
        List<Game> betMadeGames = new ArrayList<>();
        int i = 0;
        for (Game game : betGames) {
            if (availableBalance < singleBetAmount) {
                logger.noMoney();
                break;
            }
            if (!clickOnCoefficient(betCoefficient, game)) {
                game.setBetMade(-1);
                continue;
            }
            if (makeBet(singleBetAmount)) {
                availableBalance -= singleBetAmount;
                betMadeGames.add(game);
                game.setBetMade(1);
                logger.logBet(++i, singleBetAmount, betCoefficient, game, LogType.OK);
            }
        }
        return betMadeGames;
    }


    private void updateBalance() {
        if (totalBalance == -1) {
            totalBalance = (int) Double.parseDouble(waitSingleElementAndGet("top-b-acc__amount").getText());
            int balance = balanceRepository.read();
            if (totalBalance < balance) {
                totalBalance = balance;
            }
            availableBalance = totalBalance;
            balanceRepository.write(totalBalance);
        }
    }

    private int calculateAmount(RuleNumber ruleNumber) {
        double singleBetMoney = totalBalance * ruleNumber.PERCENT;
        return (int) Math.max(singleBetMoney, 20);
    }

    private boolean makeBet(int amount) {
        driver.findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        WebElement betButton = waitSingleElementAndGet("coupon-btn-group__item")
                .findElement(By.cssSelector("button"));
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", betButton);
        return okButtonClick(executor);
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

    private boolean clickOnCoefficient(BetCoefficient betCoefficient, Game game) {
        List<WebElement> gameCoefficients = fetchGameCoefficients(game);
        if (gameCoefficients.isEmpty()) {
            logger.logBet(0, 0, null, game, LogType.BET_NOT_FOUND);
            return false;
        }
        gameCoefficients.get(betCoefficient.INDEX).click();
        return true;
    }

    private List<WebElement> fetchGameCoefficients(Game game) {
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
