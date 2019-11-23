package com.zylex.betbot.service.parsing;

import com.zylex.betbot.controller.logger.ParsingConsoleLogger;
import com.zylex.betbot.exception.LeagueBotException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parsing football leagues links from the site.
 */
class LeagueParser {

    private ParsingConsoleLogger logger;

    private boolean leaguesFromFile;

    LeagueParser(ParsingConsoleLogger logger, boolean leaguesFromFile) {
        this.logger = logger;
        this.leaguesFromFile = leaguesFromFile;
    }

    /**
     * Gets links on leagues which include football matches from the site,
     * puts them into list and return.
     * @return - list of links.
     */
    List<String> processLeagueParsing() {
        try {
            return parseLeagueLinks();
        } catch (IOException e) {
            throw new LeagueBotException(e.getMessage(), e);
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
        logger.logLeague();
        return leaguesFromFile
                ? filterLinksFromFile(leagueLinks)
                : leagueLinks;
    }

    private Document connectToSite() throws IOException {
        return Jsoup.connect("https://1xstavka.ru/line/Football/")
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();
    }

    private List<String> filterLinksFromFile(List<String> leagueLinks) throws IOException {
        List<String> leagueLinksFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("external-resources/leagues_list.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(leagueLinksFromFile::add);
        }
        return leagueLinks.stream().filter(leagueLinksFromFile::contains).collect(Collectors.toList());
    }

    private boolean checkLeagueLink(String link) {
        return link.contains("Football")
                && !link.contains("Special")
                && !link.contains("Statistics");
    }
}
