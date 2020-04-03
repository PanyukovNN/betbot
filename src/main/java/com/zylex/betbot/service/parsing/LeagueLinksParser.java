package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.LeagueParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing football leagues links from the site.
 */
@Service
class LeagueLinksParser {

    /**
     * Gets links on leagues which include football matches from the site,
     * puts them into list and return.
     * @return - list of links.
     */
    List<String> processLeagueParsing() {
        try {
            return parseLeagueLinks();
        } catch (IOException e) {
            throw new LeagueParserException(e.getMessage(), e);
        }
    }

    private List<String> parseLeagueLinks() throws IOException {
        List<String> leagueLinks = new ArrayList<>();
        Document document = connectToSite();
        Elements leagueLinksElements = document.select("ul.liga_menu > li > a.link");
        for (Element element : leagueLinksElements) {
            String link = element.attr("href");
            if (checkLeagueLink(link)) {
                leagueLinks.add(link.replace("line/Football/", ""));
            }
        }
        ParsingConsoleLogger.logLeague();
        return leagueLinks;
    }

    private Document connectToSite() throws IOException {
        return Jsoup.connect("https://1xstavka.ru/line/Football/")
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();
    }

    private boolean checkLeagueLink(String link) {
        return link.contains("Football")
                && !link.contains("Special")
                && !link.contains("Statistics");
    }
}
