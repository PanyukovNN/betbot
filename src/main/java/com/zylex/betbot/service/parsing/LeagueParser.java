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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
            wait = new WebDriverWait(driver, 10);
            driver.navigate().to("https://1xstavka.ru/line/Football/");
            dayFilterClicks(day);
            return parseLeagueLinks();
        } catch (InterruptedException e) {
            throw new LeagueParserException(e.getMessage(), e);
        } finally {
            driverManager.addDriverToQueue(driver);
        }
    }

    private void dayFilterClicks(Day day) {
        waitElementsAndGet("ls-filter__name").get(1).click();
        waitElementsAndGet("chosen-container").get(1).click();
        waitElementsAndGet("active-result").get(1 + day.INDEX).click();
        waitElementsAndGet("ls-filter__btn").get(0).click();
        WebElement sportMenuElement = waitElementsAndGet("sport_menu").get(2).findElement(By.className("link"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sportMenuElement);
    }

    private List<String> parseLeagueLinks() throws InterruptedException {
        Thread.sleep(2000);
        String pageSource = driver.getPageSource();
        Document document = Jsoup.parse(pageSource);
        Elements leagueLinksElements = document.select("ul.liga_menu > li > a.link");
        List<String> leagueLinks = new ArrayList<>();
        for (Element element : leagueLinksElements) {
            String link = element.attr("href");
            if (checkLeagueLink(link)) {
                link = link.replace("line/Football/", "");
                leagueLinks.add(link);
            }
        }
        logger.logLeague();
        return leagueLinks;
    }

    private boolean checkLeagueLink(String link) {
        return link.contains("Football")
                && !link.contains("Special")
                && !link.contains("Statistics");
    }

    private List<WebElement> waitElementsAndGet(String className) {
        wait.ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(By.className(className)));
        return driver.findElements(By.className(className));
    }
}
