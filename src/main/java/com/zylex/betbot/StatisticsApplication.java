package com.zylex.betbot;

import com.zylex.betbot.controller.dao.GameDao;
import com.zylex.betbot.controller.dao.LeagueDao;
import com.zylex.betbot.exception.BetBotException;
import com.zylex.betbot.exception.StatisticsApplicationException;
import com.zylex.betbot.service.DriverManager;
import com.zylex.betbot.service.statistics.ResultScanner;
import com.zylex.betbot.service.statistics.StatisticsAnalyser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class StatisticsApplication {

    private static WebDriver driver;

    private static WebDriverWait wait;

    public static void main(String[] args) throws IOException, InterruptedException {
//        LocalDate startDate = LocalDate.of(2019, 12, 16);
//        LocalDate endDate = LocalDate.now().minusDays(1);

        DriverManager driverManager = new DriverManager();
        driver = driverManager.initiateDriver(true);
        wait = new WebDriverWait(driver, 5);
        try {
            driver.navigate().to("https://1xstavka.ru/live/Football/2036932-4x4-Dragon-League-League-X/219137944-Spartak-Team-Roma-Team/");
            driver.switchTo().frame(driver.findElement(By.className("statistic-after-game")));
            String[] scores = waitSingleElementAndGet("match-info__score").getText().split(" : ");
            System.out.println();
            System.out.println(scores[0]);
            System.out.println(scores[1]);
        } finally {
            driverManager.quitDriver();
        }

//        try (Connection connection = getConnection()) {
//            new StatisticsAnalyser(
//                new ResultScanner(
//                    new GameDao(connection)
//                ),
//                new LeagueDao(connection)
//            ).analyse(startDate, endDate);
//        } catch (SQLException e) {
//            throw new StatisticsApplicationException(e.getMessage(), e);
//        }
    }

    private static WebElement waitSingleElementAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.className(className)));
        return driver.findElement(By.className(className));
    }

    private static Connection getConnection() {
        try(InputStream inputStream = BetBotApplication.class.getClassLoader().getResourceAsStream("BetBotDb.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            final String login = property.getProperty("db.login");
            final String password = property.getProperty("db.password");
            final String url = property.getProperty("db.url");
            Class.forName("org.postgresql.Driver");
            return java.sql.DriverManager.getConnection(url, login, password);
        } catch(SQLException | IOException | ClassNotFoundException e) {
            throw new BetBotException(e.getMessage(), e);
        }
    }
}
