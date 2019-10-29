package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.controller.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BetProcessor {

    private DriverManager driverManager;

    private WebDriver driver;

    private WebDriverWait wait;

    public BetProcessor(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    public void process(EligibleGameContainer gameContainer, boolean mock) {
        try {
            ConsoleLogger.startLogMessage(LogType.BET, null);
            driverInit();
            logIn();
            processBets(gameContainer, mock);
            logOut();
        } catch (IOException | InterruptedException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            driverManager.addDriverToQueue(driver);
            driverManager.quitDrivers();
        }
    }

    private void driverInit() {
        driver = driverManager.getDriver();
        driver.quit();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1980,1020");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
        driver.navigate().to("https://1xstavka.ru/");
    }

    private void logIn() throws IOException, InterruptedException {
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/oneXBetAuth.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            driver.findElement(By.className("base_auth_form")).click();
            List<WebElement> authenticationForm = driver.findElements(By.className("c-input-material__input"));
            authenticationForm.get(0).sendKeys(property.getProperty("oneXBet.login"));
            authenticationForm.get(1).sendKeys(property.getProperty("oneXBet.password"));
            driver.findElement(By.className("auth-button")).click();
            waitPageLoading(2000);
            ConsoleLogger.writeInLine("\nLog in.");
        }
    }

    private void processBets(EligibleGameContainer gameContainer, boolean mock) throws InterruptedException {
        ConsoleLogger.writeInLine("\nProcessing bets:");
        BetCoefficient betCoefficient = gameContainer.getBetCoefficient();
        List<Game> eligibleGames = gameContainer.getEligibleGames();
        double totalMoney = Double.parseDouble(driver.findElement(By.className("top-b-acc__amount")).getText());
        int singleBetAmount = calculateAmount(totalMoney);
        double availableBalance = totalMoney;
        for (Game game : eligibleGames) {
            if (availableBalance < singleBetAmount) {
                ConsoleLogger.writeInLine("\nMoney is over.");
                break;
            }
            List<WebElement> coefficients = getGameCoefficients(game);
            if (coefficients.size() > 0) {
                coefficients.get(betCoefficient.INDEX).click();
                singleBet(singleBetAmount, mock);
                availableBalance -= singleBetAmount;
                ConsoleLogger.logBet(eligibleGames.indexOf(game) + 1, singleBetAmount, betCoefficient, game);
            }
        }
        ConsoleLogger.writeInLine("\nBets are made successfully.");
    }

    private int calculateAmount(double totalMoney) {
        double singleBetMoney = totalMoney * 0.1d;
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
        ConsoleLogger.writeErrorMessage("Did't find the game: " + game);
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
        driver.findElements(By.className("lk_header_options_item")).get(4).click();
        driver.findElement(By.className("swal2-confirm")).click();
        ConsoleLogger.writeInLine("\nLog out.");
    }

    private void waitPageLoading(int millis) throws InterruptedException {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Thread.sleep(millis);
    }
}
