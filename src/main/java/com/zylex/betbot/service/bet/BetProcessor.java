package com.zylex.betbot.service.bet;

import com.zylex.betbot.controller.logger.BetConsoleLogger;
import com.zylex.betbot.controller.logger.LogType;
import com.zylex.betbot.exception.BetProcessorException;
import com.zylex.betbot.model.*;
import com.zylex.betbot.model.bet.Bet;
import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.bet.BetStatus;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.driver.DriverManager;
import com.zylex.betbot.service.repository.*;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.zylex.betbot.BetBotApplication.BET_START_TIME;
import static com.zylex.betbot.BetBotApplication.BOT_START_TIME;

/**
 * Making bets.
 */
@Service
@PropertySource(value = "classpath:application.properties")
public class BetProcessor {

    private final BetConsoleLogger logger = new BetConsoleLogger();

    private final DriverManager driverManager;

    private final BankRepository bankRepository;

    private final GameRepository gameRepository;

    private final BetRepository betRepository;

    private final RuleRepository ruleRepository;

    private final RuleProcessor ruleProcessor;

    private int totalBalance = -1;

    private int availableBalance = -1;

    @Value("${site.login}")
    private String login;

    @Value("${site.password}")
    private String password;

    @Autowired
    public BetProcessor(DriverManager driverManager,
                        BankRepository bankRepository,
                        GameRepository gameRepository,
                        BetRepository betRepository,
                        RuleRepository ruleRepository,
                        RuleProcessor ruleProcessor) {
        this.driverManager = driverManager;
        this.bankRepository = bankRepository;
        this.gameRepository = gameRepository;
        this.betRepository = betRepository;
        this.ruleRepository = ruleRepository;
        this.ruleProcessor = ruleProcessor;
    }

    /**
     * Initiates web driver, opens the site, logs in,
     * makes bets, and saves bets to database.
     */
    @Transactional
    public List<Game> process() {
        try {
            List<Rule> rules = ruleRepository.findByActivateTrue();
            Set<Game> betGames = findBetGames(rules);
            if (betGames.isEmpty()) {
                logger.betMade(LogType.NO_GAMES_TO_BET);
                return Collections.emptyList();
            }
            openSite();
            logger.startLogMessage(LogType.BET);
            List<Game> processedGames = new ArrayList<>();
            for (Game game : betGames) {
                if (!enoughMoney(rules, game)) {
                    logger.noMoney();
                    break;
                }
                processGameBet(rules, game);
                processedGames.add(game);
            }
            logger.betMade(LogType.OK);
            return processedGames;
        } catch (ElementNotInteractableException e) {
            throw new BetProcessorException(e.getMessage(), e);
        }
    }

    private Set<Game> findBetGames(List<Rule> rules) {
        List<Game> ruleGames = ruleProcessor.filterGamesByRules(gameRepository.findByBetStartTime(), rules);
        Set<Game> betGames = new LinkedHashSet<>();
        for (Game game : ruleGames) {
            if (notAppropriateTime(game)) continue;
            if (game.getBets().stream().anyMatch(bet -> rules.contains(bet.getRule()))) continue;
            betGames.add(game);
        }
        return betGames;
    }

    private boolean notAppropriateTime(Game game) {
        return BOT_START_TIME.isBefore(LocalDateTime.of(game.getDateTime().toLocalDate().minusDays(1), BET_START_TIME))
                || BOT_START_TIME.isAfter(game.getDateTime());
    }

    private void openSite() {
        if (driverManager.getDriver() != null) return;
        driverManager.initiateDriver();
        driverManager.getDriver().navigate().to("https://1xstavka.ru/");
        logIn();
    }

    private void logIn() {
        logger.startLogMessage(LogType.LOG_IN);
        driverManager.waitElement(By::className, "base_auth_form").click();
        List<WebElement> authenticationForm = driverManager.waitElements(By::className, "c-input-material__input");
        authenticationForm.get(0).sendKeys(login);
        authenticationForm.get(1).sendKeys(password);
        driverManager.waitElement(By::className, "auth-button").click();
        checkVerify();
        updateBalance();
    }

    private void checkVerify() {
        try {
            driverManager.waitElement(By::className, "top-b-acc__amount");
            logger.logInLog(LogType.OK);
        } catch (TimeoutException e) {
            logger.logInLog(LogType.VERIFY);
        }
    }

    private void updateBalance() {
        if (totalBalance == -1) {
            availableBalance = (int) Double.parseDouble(driverManager.waitElement(By::className, "top-b-acc__amount").getText());
            totalBalance = availableBalance;
            bankRepository.save(new Bank(LocalDateTime.now(), totalBalance));
        }
    }

    private boolean enoughMoney(List<Rule> rules, Game game) {
        List<Rule> gameRules = rules.stream()
                .filter(rule -> game.getRules().contains(rule))
                .collect(Collectors.toList());
        return availableBalance >= calculateAmount(gameRules);
    }

    private void processGameBet(List<Rule> rules, Game game) {
        for (Rule rule : rules) {
            if (!game.getRules().contains(rule)) continue;
            if (game.getBets().stream().anyMatch(bet -> bet.getRule().equals(rule))) continue;
            int ruleBetAmount = calculateAmount(rule);
            List<BetCoefficient> betCoefficients = Arrays.stream(rule.getBetCoefficient().split("__"))
                    .map(BetCoefficient::valueOf)
                    .collect(Collectors.toList());
            List<Bet> gameBet = new ArrayList<>();
            for (BetCoefficient betCoefficient : betCoefficients) {
                gameBet.add(betOnCoefficient(game, rule, ruleBetAmount, betCoefficient));
            }
            logger.logBet(game, gameBet);
        }
    }

    private Bet betOnCoefficient(Game game, Rule rule, int ruleBetAmount, BetCoefficient betCoefficient) {
        if (!clickOnCoefficient(betCoefficient, game)) {
            return saveBet(game, new Bet(LocalDateTime.now(), game, rule, BetStatus.FAIL.toString(), 0, BetCoefficient.NONE.toString()));
        } else if (makeBet(ruleBetAmount)) {
            availableBalance -= ruleBetAmount;
            return saveBet(game, new Bet(LocalDateTime.now(), game, rule, BetStatus.SUCCESS.toString(), ruleBetAmount, betCoefficient.toString()));
        } else {
            return saveBet(game, new Bet(LocalDateTime.now(), game, rule, BetStatus.ERROR.toString(), 0, BetCoefficient.NONE.toString()));
        }
    }

    private Bet saveBet(Game game, Bet bet) {
        bet = betRepository.save(bet);
        game.getBets().add(bet);
        //TODO check !!!
//        gameRepository.update(game);
        return bet;
    }

    private int calculateAmount(Rule rule) {
        double singleBetMoney = totalBalance * rule.getPercent();
        return (int) Math.max(singleBetMoney, 20);
    }

    private int calculateAmount(List<Rule> rules) {
        int sum = 0;
        for (Rule rule : rules) {
            sum += calculateAmount(rule);
        }
        return sum;
    }

    private boolean makeBet(int amount) {
//        driverManager.getDriver().findElement(By.className("bet_sum_input")).sendKeys(String.valueOf(amount));
//        WebElement betButton = driverManager.waitElement(By::className, "coupon-btn-group__item")
//                .findElement(By.cssSelector("button"));
//        JavascriptExecutor executor = (JavascriptExecutor) driverManager.getDriver();
//        executor.executeScript("arguments[0].click();", betButton);
//        return okButtonClick(executor);
        return true;
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
            return false;
        }
        return true;
    }

    private boolean clickOnCoefficient(BetCoefficient betCoefficient, Game game) {
        List<WebElement> gameCoefficients = fetchGameCoefficients(game);
        if (gameCoefficients.isEmpty()) {
            return false;
        }
        try {
            gameCoefficients.get(betCoefficient.INDEX).click();
            return true;
        } catch (ElementClickInterceptedException e) {
            return false;
        }
    }

    private List<WebElement> fetchGameCoefficients(Game game) {
        String gameUrl = "https://1xstavka.ru/line/Football/" + game.getLeague().getLink();
        if (!gameUrl.equals(driverManager.getDriver().getCurrentUrl())) {
            driverManager.getDriver().navigate().to(gameUrl);
        }
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
        return Collections.emptyList();
    }

    private LocalDateTime processDateTime(WebElement gameElement) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String year = String.valueOf(LocalDate.now().getYear());
        String dateTime = gameElement.findElement(By.className("c-events__time"))
                .findElement(By.cssSelector("span"))
                .getText()
                .replace(" ", String.format(".%s ", year)).substring(0, 16);
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
    }
}
