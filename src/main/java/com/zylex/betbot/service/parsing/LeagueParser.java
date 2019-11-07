package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.LeagueBotException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing football leagues links, which includes game for a next day on site.
 */
@SuppressWarnings("WeakerAccess")
public class LeagueParser {

    private ParsingConsoleLogger logger;

    public LeagueParser(ParsingConsoleLogger logger) {
        this.logger = logger;
    }

    /**
     * Gets links on leagues which include football matches for a specified day from site,
     * puts them into list and return.
     * @return - list of links.
     */
    public List<String> processLeagueParsing() {
        try {
            return parseLeagueLinks();
        } catch (IOException e) {
            throw new LeagueBotException(e.getMessage(), e);
        }
    }

    private List<String> parseLeagueLinks() throws IOException {
        Document document = Jsoup.connect("https://1xstavka.ru/line/Football/")
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();
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
}
