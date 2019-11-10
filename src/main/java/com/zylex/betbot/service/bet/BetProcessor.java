package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.BetRepository;
import com.zylex.betbot.controller.ParsingRepository;
import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.GameContainer;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Making bets.
 */
public class BetProcessor {

    private BetConsoleLogger logger = new BetConsoleLogger();

    private WebDriver driver;

    private WebDriverWait wait;

    private RuleNumber ruleNumber;

    private RuleProcessor ruleProcessor;

    private BetRepository betRepository;

    private boolean mock;

    private boolean doBet;

    public BetProcessor(RuleProcessor ruleProcessor, BetRepository betRepository, RuleNumber ruleNumber, boolean mock, boolean doBet) {
        this.ruleProcessor = ruleProcessor;
        this.betRepository = betRepository;
        this.ruleNumber = ruleNumber;
        this.mock = mock;
        this.doBet = doBet;
    }

    /**
     * Initiates one non-headless chrome driver, navigates to site,
     * logs in, makes bets and log out.
     */
    public void process() {
        GameContainer gameContainer = ruleProcessor.process();
        try {
            if (gameContainer.getEligibleGames().get(ruleNumber).size() > 0
                    && !doBet) {
                logger.betMade(LogType.ERROR);
                return;
            }
            driverInit();
            logger.logRule(ruleNumber);
            logger.startLogMessage(LogType.LOG_IN);
            if (logIn()) {
                logger.startLogMessage(LogType.BET);
                processBets(gameContainer);
            }
        } catch (IOException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void driverInit() {
        DriverManager driverManager = new DriverManager();
        driverManager.initiateDriver(false);
        driver = driverManager.getDriver();
        wait = new WebDriverWait(driver, 10);
        driver.navigate().to("https://1xstavka.ru/");
    }

    private boolean logIn() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("oneXBetAuth.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            waitSingleElementAndGet("base_auth_form").click();
            List<WebElement> authenticationForm = waitElementsAndGet("c-input-material__input");
            authenticationForm.get(0).sendKeys(property.getProperty("oneXBet.login"));
            authenticationForm.get(1).sendKeys(property.getProperty("oneXBet.password"));
            waitSingleElementAndGet("auth-button").click();
            String url = driver.getCurrentUrl();
            logger.logInLog(LogType.OK);
            if (url.contains("accountverify")) {
                logger.logInLog(LogType.ERROR);
                return false;
            }
            return true;
        }
    }

    private void processBets(GameContainer gameContainer) throws IOException {
        List<Game> eligibleGames = gameContainer.getEligibleGames().get(ruleNumber);
        BetCoefficient betCoefficient = ruleNumber.betCoefficient;
        double totalMoney = Double.parseDouble(waitSingleElementAndGet("top-b-acc__amount").getText());
        int singleBetAmount = calculateAmount(betCoefficient, totalMoney);
        double availableBalance = totalMoney;
        List<Game> betMadeGames = betRepository.readBetMadeFile();
        int i = 0;
        for (Game game : eligibleGames) {
            if (betMadeGames.contains(game)) {
                continue;
            }
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
                } else {
                    //TODO log error
                }
            }
        }
        logger.betMade(LogType.OK);
        betRepository.saveBetMadeGamesToFile(betMadeGames);
        if (!mock && doBet) {
            betRepository.saveTotalBetGamesToFile(betMadeGames);
        }
    }

    private int calculateAmount(BetCoefficient betCoefficient, double totalMoney) {
        double singleBetMoney = totalMoney * betCoefficient.PERCENT;
        return (int) Math.max(singleBetMoney, 20);
    }

    private boolean makeBet(double amount, boolean mock) {
        driver.findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        if (!mock) {
            WebElement betButton = waitSingleElementAndGet("coupon-btn-group__item")
                    .findElement(By.cssSelector("button"));
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", betButton);
            try {
                WebElement okButton = waitSingleElementAndGet("o-btn-group__item")
                        .findElement(By.cssSelector("button"));
                executor.executeScript("arguments[0].click();", okButton);
            } catch (TimeoutException e) {
                //TODO error Ok button, process error games
                WebElement okButton = waitSingleElementAndGet("swal2-confirm");
                executor.executeScript("arguments[0].click();", okButton);
                WebElement delButton = waitSingleElementAndGet("c-bet-box__del");
                executor.executeScript("arguments[0].click();", delButton);
                return false;
            }
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
        logger.logBet(0, 0, null, game, LogType.ERROR);
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
