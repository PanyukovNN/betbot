package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.*;
import com.zylex.betbot.model.bet.Bet;
import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.bet.BetInfo;
import com.zylex.betbot.model.bet.BetStatus;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.driver.DriverManager;
import com.zylex.betbot.service.repository.*;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.zylex.betbot.BetBotApplication.betStartTime;
import static com.zylex.betbot.BetBotApplication.botStartTime;

/**
 * Making bets.
 */
@Service
public class BetProcessor {

    private BetConsoleLogger logger = new BetConsoleLogger();

    private DriverManager driverManager;

    private BankRepository bankRepository;

    private BetInfoRepository betInfoRepository;

    private GameRepository gameRepository;

    private RuleRepository ruleRepository;

    private BetRepository betRepository;

    private int totalBalance = -1;

    private int availableBalance = -1;

    @Autowired
    public BetProcessor(DriverManager driverManager,
                        BankRepository bankRepository,
                        GameRepository gameRepository,
                        BetInfoRepository betInfoRepository,
                        RuleRepository ruleRepository,
                        BetRepository betRepository) {
        this.driverManager = driverManager;
        this.bankRepository = bankRepository;
        this.gameRepository = gameRepository;
        this.betInfoRepository = betInfoRepository;
        this.ruleRepository = ruleRepository;
        this.betRepository = betRepository;
    }

    /**
     * Initiates non-headless chrome driver, opens the site, logs in,
     * makes bets, and saves bet made games to database.
     */
    @Transactional
    public void process(Map<Rule, List<Game>> ruleGames, List<String> ruleNames) {
        try {
            List<Rule> rules = ruleRepository.getAll();
            for (Rule rule : rules) {
                if (!ruleNames.contains(rule.getName())) continue;
                List<Game> games = ruleGames.get(rule);
                List<Game> betGames = findBetGames(games, rule);
                if (!betGames.isEmpty()) {
                    openSite();
                    logIn();
                    processBets(rule, betGames);
                }
            }
            if (driverManager.getDriver() == null) {
                logger.betMade(LogType.NO_GAMES_TO_BET);
            } else {
                betInfoRepository.save(new BetInfo(botStartTime));
                logger.betMade(LogType.OK);
            }
        } catch (ElementNotInteractableException | IOException e) {
            throw new BetProcessorException(e.getMessage(), e);
        }
    }

    private List<Game> findBetGames(List<Game> betGames, Rule rule) {
        return filterByBetNotMade(filterByTime(betGames), rule);
    }

    private List<Game> filterByBetNotMade(List<Game> filteredBetGames, Rule rule) {
        return filteredBetGames.stream()
                .filter(game -> game.getBets().stream()
                        .noneMatch(bet -> bet.getRule().equals(rule)))
                .collect(Collectors.toList());
    }

    private List<Game> filterByTime(List<Game> betGames) {
        return betGames.stream()
                .filter(game -> botStartTime.isAfter(LocalDateTime.of(game.getDateTime().toLocalDate().minusDays(1), betStartTime)))
                .filter(game -> botStartTime.isBefore(game.getDateTime()))
                .collect(Collectors.toList());
    }

    private void openSite() {
        if (driverManager.getDriver() == null) {
            driverManager.initiateDriver(false);
            driverManager.getDriver().navigate().to("https://1xstavka.ru/");
        }
    }

    private void logIn() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("BetBotAuth.properties")) {
            logger.startLogMessage(LogType.LOG_IN, "");
            Properties property = new Properties();
            property.load(inputStream);
            driverManager.waitElement(By::className, "base_auth_form").click();
            List<WebElement> authenticationForm = driverManager.waitElements(By::className, "c-input-material__input");
            authenticationForm.get(0).sendKeys(property.getProperty("BetBot.login"));
            authenticationForm.get(1).sendKeys(property.getProperty("BetBot.password"));
            driverManager.waitElement(By::className, "auth-button").click();
            checkVerify();
        }
    }

    private void checkVerify() {
        try {
            driverManager.waitElement(By::className, "top-b-acc__amount");
            logger.logInLog(LogType.OK);
        } catch (TimeoutException e) {
            logger.logInLog(LogType.VERIFY);
        }
    }

    private void processBets(Rule rule, List<Game> betGames) {
        logger.startLogMessage(LogType.BET, rule.getName());
        BetCoefficient betCoefficient = BetCoefficient.valueOf(rule.getBetCoefficient());
        updateBalance();
        int singleBetAmount = calculateAmount(rule);
        int i = 0;
        for (Game game : betGames) {
            if (availableBalance < singleBetAmount) {
                logger.noMoney();
                break;
            }
            if (!clickOnCoefficient(betCoefficient, game)) {
                Bet bet = betRepository.save(
                        new Bet(LocalDateTime.now(), game, rule, BetStatus.FAIL.toString(), 0, BetCoefficient.NONE.toString()));
                game.getBets().add(bet);
                gameRepository.update(game);
                logger.logBet(++i, 0, null, game, LogType.BET_NOT_FOUND);
            } else if (makeBet(singleBetAmount)) {
                availableBalance -= singleBetAmount;
                Bet bet = betRepository.save(
                        new Bet(LocalDateTime.now(), game, rule, BetStatus.SUCCESS.toString(), singleBetAmount, rule.getBetCoefficient()));
                game.getBets().add(bet);
                gameRepository.update(game);
                logger.logBet(++i, singleBetAmount, betCoefficient, game, LogType.OK);
            }
        }
    }

    private void updateBalance() {
        if (totalBalance == -1) {
            availableBalance = (int) Double.parseDouble(driverManager.waitElement(By::className, "top-b-acc__amount").getText());
            totalBalance = availableBalance;
            bankRepository.save(new Bank(LocalDate.now(), totalBalance));
        }
    }

    private int calculateAmount(Rule rule) {
        double singleBetMoney = totalBalance * rule.getPercent();
        return (int) Math.max(singleBetMoney, 20);
    }

    private boolean makeBet(int amount) {
        driverManager.getDriver().findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
        WebElement betButton = driverManager.waitElement(By::className, "coupon-btn-group__item")
                .findElement(By.cssSelector("button"));
        JavascriptExecutor executor = (JavascriptExecutor) driverManager.getDriver();
        executor.executeScript("arguments[0].click();", betButton);
        return okButtonClick(executor);
    }

    private boolean okButtonClick(JavascriptExecutor executor) {
        try {
            WebElement okButton = driverManager.waitElement(By::className, "o-btn-group__item")
                    .findElement(By.cssSelector("button"));
            executor.executeScript("arguments[0].click();", okButton);
        } catch (TimeoutException e) {
            WebElement okButton = driverManager.waitElement(By::className, "swal2-confirm");
            executor.executeScript("arguments[0].click();", okButton);
            WebElement delButton = driverManager.waitElement(By::className, "c-bet-box__del");
            executor.executeScript("arguments[0].click();", delButton);
            logger.logBet(0, 0, null, null, LogType.BET_ERROR);
            return false;
        }
        return true;
    }

    private boolean clickOnCoefficient(BetCoefficient betCoefficient, Game game) {
        List<WebElement> gameCoefficients = fetchGameCoefficients(game);
        if (gameCoefficients.isEmpty()) {
            return false;
        }
        gameCoefficients.get(betCoefficient.INDEX).click();
        return true;
    }

    private List<WebElement> fetchGameCoefficients(Game game) {
        driverManager.getDriver().navigate().to("https://1xstavka.ru/line/Football/" + game.getLeague().getLink());
        List<WebElement> gameWebElements = driverManager.waitElements(By::className, "c-events__item_game");
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
}
