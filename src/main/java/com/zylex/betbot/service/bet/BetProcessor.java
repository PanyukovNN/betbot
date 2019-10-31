package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Making bets.
 */
public class BetProcessor {

    private DriverManager driverManager;

    private WebDriver driver;

    private WebDriverWait wait;

    private GameContainer gameContainer;

    private RuleNumber ruleNumber;

    private BetConsoleLogger logger = new BetConsoleLogger();

    public BetProcessor(GameContainer gameContainer, RuleNumber ruleNumber) {
        this.gameContainer = gameContainer;
        this.ruleNumber = ruleNumber;
    }

    /**
     * Initiates one non-headless chrome driver, navigates to site,
     * logs in, makes bets and log out.
     * @param mock - flag for doing mock bets.
     */
    public void process(boolean mock, boolean doBets) {
        if (!doBets) {
            logger.betsMade(LogType.ERROR);
            return;
        }
        try {
            driverInit();
            logger.logRule(ruleNumber);
            logger.startLogMessage(LogType.LOG_IN, null);
            if (logIn()) {
                logger.startLogMessage(LogType.BET, null);
                processBets(gameContainer, mock);
                logger.startLogMessage(LogType.LOG_OUT, null);
                logOut();
            }
        } catch (IOException | InterruptedException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            driverManager.addDriverToQueue(driver);
            driverManager.quitDrivers();
        }
    }

    private void driverInit() {
        System.out.println();
        driverManager = new DriverManager(1);
        driverManager.initiateDrivers(false);
        driver = driverManager.getDriver();
        wait = new WebDriverWait(driver, 10);
        driver.navigate().to("https://1xstavka.ru/");
    }

    private boolean logIn() throws IOException, InterruptedException {
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/oneXBetAuth.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            driver.findElement(By.className("base_auth_form")).click();
            List<WebElement> authenticationForm = driver.findElements(By.className("c-input-material__input"));
            authenticationForm.get(0).sendKeys(property.getProperty("oneXBet.login"));
            authenticationForm.get(1).sendKeys(property.getProperty("oneXBet.password"));
            driver.findElement(By.className("auth-button")).click();
            waitPageLoading(2000);
            String url = driver.getCurrentUrl();
            logger.logInLog(LogType.OK);
            if (url.contains("accountverify")) {
                logger.logInLog(LogType.ERROR);
                return false;
            }
            return true;
        }
    }

    private void processBets(GameContainer gameContainer, boolean mock) throws InterruptedException {
        waitPageLoading(1000);
        List<Game> eligibleGames = gameContainer.getEligibleGames().get(ruleNumber);
        BetCoefficient betCoefficient = ruleNumber.betCoefficient;
        double totalMoney = Double.parseDouble(driver.findElement(By.className("top-b-acc__amount")).getText());
        int singleBetAmount = calculateAmount(betCoefficient, totalMoney);
        double availableBalance = totalMoney;
        for (Game game : eligibleGames) {
            if (availableBalance < singleBetAmount) {
                logger.noMoney();
                break;
            }
            List<WebElement> coefficients = getGameCoefficients(game);
            if (coefficients.size() > 0) {
                coefficients.get(betCoefficient.INDEX).click();
                singleBet(singleBetAmount, mock);
                availableBalance -= singleBetAmount;
                logger.logBet(eligibleGames.indexOf(game) + 1, singleBetAmount, betCoefficient, game, LogType.OK);
            }
        }
        logger.betsMade(LogType.OK);
    }

    private int calculateAmount(BetCoefficient betCoefficient, double totalMoney) {
        double singleBetMoney = totalMoney * betCoefficient.PERCENT;
        return (int) Math.max(singleBetMoney, 20);
    }

    private void singleBet(double amount, boolean mock) throws InterruptedException {
        driver.findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        if (!mock) {
            WebElement betButton = driver.findElement(By.className("coupon-btn-group__item")).findElement(By.cssSelector("button"));
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", betButton);
            waitPageLoading(1500);
            WebElement okButton = driver.findElement(By.className("o-btn-group__item")).findElement(By.cssSelector("button"));
            executor.executeScript("arguments[0].click();", okButton);
            waitPageLoading(500);
        }
    }

    private List<WebElement> getGameCoefficients(Game game) throws InterruptedException {
        driver.navigate().to(game.getLeagueLink());
        waitPageLoading(500);
        List<WebElement> gameWebElements = driver.findElements(By.className("c-events__item_game"));
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
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        dateTime = dateTime.replace(" ", String.format(".%s ", year)).substring(0, 16);
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }

    private void logOut() throws InterruptedException {
        waitPageLoading(3000);
        WebElement lkWrap = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("wrap_lk")));
        Actions actions = new Actions(driver);
        actions.moveToElement(lkWrap).build().perform();
        waitPageLoading(1000);
        driver.findElements(By.className("lk_header_options_item")).get(4).click();
        waitPageLoading(1000);
        driver.findElement(By.className("swal2-confirm")).click();
        logger.logOutLog(LogType.OK);
    }

    private void waitPageLoading(int millis) throws InterruptedException {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Thread.sleep(millis);
    }
}
