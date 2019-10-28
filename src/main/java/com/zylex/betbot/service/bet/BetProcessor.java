package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.ConsoleLogger;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BetProcessor {

    private DriverManager driverManager = new DriverManager();

    private WebDriver driver;

    private WebDriverWait wait;

    public void process(EligibleGameContainer gameContainer, boolean mock) {
        try {
            driverManager.initiateDrivers(1, false);
            driver = driverManager.getDriver();
            wait = new WebDriverWait(driver, 2);
            driver.navigate().to("https://1xstavka.ru/");
            logIn();
            makeBets(gameContainer, mock);
            logOut();
        } catch (IOException | InterruptedException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            driverManager.addDriverToQueue(driver);
            driverManager.quitDrivers();
        }
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
        }
    }

    private void makeBets(EligibleGameContainer gameContainer, boolean mock) throws InterruptedException {
        BetCoefficient betCoefficient = gameContainer.getBetCoefficient();
        List<Game> eligibleGames = gameContainer.getEligibleGames();
        for (Game game : eligibleGames) {
            List<WebElement> coefficients = getGameCoefficients(game);
            if (coefficients.size() > 0) {
                coefficients.get(betCoefficient.index).click();
                double amount = calculateAmount(eligibleGames.size());
                singleBet(amount, mock);
                ConsoleLogger.writeInLine(String.format("\n%d) A bet for %s rub. has been placed on %s for: %s",
                        eligibleGames.indexOf(game) + 1,
                        new DecimalFormat("#.00").format(amount).replace(",", "."),
                        betCoefficient,
                        game));
            }
        }
    }

    private int calculateAmount(int gameNumber) {
        double totalMoney = Double.parseDouble(driver.findElement(By.className("top-b-acc__amount")).getText());
        int singleBetMoney = (int) totalMoney / gameNumber;
        int tenPercentBet = (int) totalMoney / 10;
        return Math.max(Math.min(singleBetMoney, tenPercentBet), 20);
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
        driver.navigate().to(String.format("https://1xstavka.ru/%s", game.getLeagueLink()));
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
        waitPageLoading(2000);
        WebElement lkWrap = driver.findElement(By.className("wrap_lk"));
        Actions actions = new Actions(driver);
        actions.moveToElement(lkWrap).build().perform();
        driver.findElements(By.className("lk_header_options_item")).get(4).click();
        driver.findElement(By.className("swal2-confirm")).click();
    }

    private void waitPageLoading(int millis) throws InterruptedException {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Thread.sleep(millis);
    }
}
