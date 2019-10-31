package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.LeagueParserException;
import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing football leagues links, which includes game for a next day on site.
 */
@SuppressWarnings("WeakerAccess")
public class LeagueParser {

    private ParsingConsoleLogger logger;

    private DriverManager driverManager;

    private WebDriverWait wait;

    private WebDriver driver;

    public LeagueParser(ParsingConsoleLogger logger, DriverManager driverManager) {
        this.logger = logger;
        this.driverManager = driverManager;
    }

    /**
     * Gets links on leagues which include football matches for a specified day from site,
     * puts them into list and return.
     * @param day - what day to parse.
     * @return - list of links.
     */
    public List<String> processLeagueParsing(Day day) {
        try {
            driver = driverManager.getDriver();
            wait = new WebDriverWait(driver, 60);
            driver.navigate().to("https://1xstavka.ru/line/Football/");
            dayFilterClicks(day);
            return parseLeagueLinks();
        } catch (InterruptedException | IOException e) {
            throw new LeagueParserException(e.getMessage(), e);
        } finally {
            driverManager.addDriverToQueue(driver);
        }
    }

    private void dayFilterClicks(Day day) throws InterruptedException {
        guaranteedClick("ls-filter__name",1);
        guaranteedClick("chosen-container",1);
        guaranteedClick("active-result",1 + day.INDEX);
        guaranteedClick("ls-filter__btn",0);
        WebElement sprotMenuElement = driver.findElements(By.className("sport_menu")).get(2).findElement(By.className("link"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sprotMenuElement);
    }

    private void guaranteedClick(String className, int index) throws InterruptedException {
        while (true) {
            try {
                wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
                Thread.sleep(1000);
                if (driver.findElements(By.className(className)).size() > 0) {
                    driver.findElements(By.className(className)).get(index).click();
                }
                break;
            } catch (NoSuchElementException | StaleElementReferenceException | ElementClickInterceptedException e) {
//                System.out.println("Can't click, trying again...");
            }
        }
    }

    private List<String> parseLeagueLinks() throws InterruptedException, IOException {
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        Thread.sleep(2000);
        String pageSource = driver.getPageSource();
        Document document = Jsoup.parse(pageSource);
        Elements leagueLinksElements = document.select("ul.liga_menu > li > a.link");
        List<String> leagueLinks = new ArrayList<>();
        for (Element element : leagueLinksElements) {
            String link = element.attr("href");
            if (checkLeagueLink(link)) {
                leagueLinks.add(link);
            }

        }
        logger.logLeague();
        return leagueLinks;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean checkLeagueLink(String link) throws IOException {
        File file = new File(this.getClass().getClassLoader().getResource("exclude_leagues.txt").getFile());
        List<String> excludeLeagues = Files.readAllLines(file.toPath());
        for (String excludeLeague : excludeLeagues) {
            if (link.contains(excludeLeague)) {
                return false;
            }
        }
        return link.contains("Football")
                && !link.contains("Special")
                && !link.contains("Statistics");
    }
}
