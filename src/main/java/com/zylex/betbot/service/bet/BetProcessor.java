package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.Repository;
import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.BetCoefficient;
import com.zylex.betbot.model.GameContainer;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameResult;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.bet.rule.RuleNumber;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Making bets.
 */
public class BetProcessor {

    private BetConsoleLogger logger = new BetConsoleLogger();

    private DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private WebDriver driver;

    private WebDriverWait wait;

    private RuleNumber ruleNumber;

    private Repository repository;

    public BetProcessor(Repository repository, RuleNumber ruleNumber) {
        this.repository = repository;
        this.ruleNumber = ruleNumber;
    }

    /**
     * Initiates one non-headless chrome driver, navigates to site,
     * logs in, makes bets and log out.
     *
     * @param mock - flag for doing mock bets.
     */
    public void process(boolean mock, boolean doBets) {
        GameContainer gameContainer = repository.processSaving();
        try {
            driverInit();
            if (gameContainer.getEligibleGames().get(ruleNumber).size() > 0
                    && !doBets) {
                logger.betsMade(LogType.ERROR);
                return;
            }
            logger.logRule(ruleNumber);
            logger.startLogMessage(LogType.LOG_IN);
            if (logIn()) {
                logger.startLogMessage(LogType.BET);
                processBets(gameContainer, mock);
            }
        } catch (IOException | ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        } finally {
            driver.quit();
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

    private void processBets(GameContainer gameContainer, boolean mock) throws IOException {
        List<Game> eligibleGames = gameContainer.getEligibleGames().get(ruleNumber);
        BetCoefficient betCoefficient = ruleNumber.betCoefficient;
        double totalMoney = Double.parseDouble(waitSingleElementAndGet("top-b-acc__amount").getText());
        int singleBetAmount = calculateAmount(betCoefficient, totalMoney);
        double availableBalance = totalMoney;
        List<Game> betsMadeGames = readBetsMadeGames();
        int i = 0;
        for (Game game : eligibleGames) {
            if (betsMadeGames.contains(game)) {
                continue;
            }
            if (availableBalance < singleBetAmount) {
                logger.noMoney();
                break;
            }
            List<WebElement> coefficients = getGameCoefficients(game);
            if (coefficients.size() > 0) {
                coefficients.get(betCoefficient.INDEX).click();
                makeBet(singleBetAmount, mock);
                availableBalance -= singleBetAmount;
                betsMadeGames.add(game);
                logger.logBet(++i, singleBetAmount, betCoefficient, game, LogType.OK);
            }
        }
        saveBetsMadeGamesToFile(betsMadeGames);
        logger.betsMade(LogType.OK);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private List<Game> readBetsMadeGames() throws IOException {
        List<Game> betsMadeGames = new ArrayList<>();
        File file = new File(String.format("results/%s/%s/BETS_MADE_%s.csv", repository.getMonthDirName(), repository.getDirName(), repository.getDirName()));
        if (!file.exists()) {
            file.createNewFile();
        }
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            String[] fields = line.split(";");
            Game game = new Game(fields[0], fields[1], LocalDateTime.parse(fields[2] + ";" + fields[3], DATE_FORMATTER),
                    fields[4], fields[5], RuleNumber.valueOf(fields[6]), GameResult.NO_RESULT);
            if (game.getDateTime().isAfter(LocalDateTime.now().minusDays(1))) {
                betsMadeGames.add(game);
            }
        }
        return betsMadeGames;
    }

    private void saveBetsMadeGamesToFile(List<Game> madeBetsGames) throws IOException {
        File file = new File(String.format("results/%s/%s/BETS_MADE_%s.csv", repository.getMonthDirName(), repository.getDirName(), repository.getDirName()));
        File totalBetsMadeFile = new File(String.format("results/%s/BETS_MADE_%s.csv", repository.getMonthDirName(), repository.getMonthDirName()));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
             BufferedWriter totalBetsMadeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(totalBetsMadeFile, true), StandardCharsets.UTF_8))) {
            String MADE_BET_GAME_FORMAT = "%s;%s;%s;%s;%s;%s;%s\n";
            for (Game game : madeBetsGames) {
                String line = String.format(MADE_BET_GAME_FORMAT,
                        game.getLeague(),
                        game.getLeagueLink(),
                        DATE_FORMATTER.format(game.getDateTime()),
                        game.getFirstTeam(),
                        game.getSecondTeam(),
                        game.getRuleNumber(),
                        game.getGameResult());
                writer.write(line);
                totalBetsMadeWriter.write(line);
            }
        }
    }

    private int calculateAmount(BetCoefficient betCoefficient, double totalMoney) {
        double singleBetMoney = totalMoney * betCoefficient.PERCENT;
        return (int) Math.max(singleBetMoney, 20);
    }

    private void makeBet(double amount, boolean mock) {
        driver.findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        if (!mock) {
            WebElement betButton = waitSingleElementAndGet("coupon-btn-group__item")
                    .findElement(By.cssSelector("button"));
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", betButton);
            WebElement okButton = waitSingleElementAndGet("o-btn-group__item")
                    .findElement(By.cssSelector("button"));
            executor.executeScript("arguments[0].click();", okButton);
        }
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
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElements(By.className(className));
    }

    private WebElement waitSingleElementAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElement(By.className(className));
    }
}
