package com.zylex.betbot.model;

import com.zylex.betbot.service.rule.RuleNumber;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Instance of a football game.
 */
public class Game {

    private long id;

    private String league;

    private LocalDateTime dateTime;

    private String firstTeam;

    private String secondTeam;

    private double firstWin;

    private double tie;

    private double secondWin;

    private double firstWinOrTie;

    private double secondWinOrTie;

    private GameResult gameResult;

    private int betMade = 0;

    private String link;

    private String leagueLink;

    private RuleNumber ruleNumber;
    
    public Game(long id, String league, LocalDateTime dateTime, String firstTeam, String secondTeam, double firstWin, double tie, double secondWin, double firstWinOrTie, double secondWinOrTie, GameResult gameResult, int betMade, String link, String leagueLink, RuleNumber ruleNumber) {
        this.id = id;
        this.league = league;
        this.dateTime = dateTime;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.firstWin = firstWin;
        this.tie = tie;
        this.secondWin = secondWin;
        this.firstWinOrTie = firstWinOrTie;
        this.secondWinOrTie = secondWinOrTie;
        this.gameResult = gameResult;
        this.betMade = betMade;
        this.link = link;
        this.leagueLink = leagueLink;
        this.ruleNumber = ruleNumber;
    }

    public Game() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLeague() {
        return league;
    }

    public String getLeagueLink() {
        return leagueLink;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFirstTeam() {
        return firstTeam;
    }

    public String getSecondTeam() {
        return secondTeam;
    }

    public double getFirstWin() {
        return firstWin;
    }

    public double getTie() {
        return tie;
    }

    public double getSecondWin() {
        return secondWin;
    }

    public double getFirstWinOrTie() {
        return firstWinOrTie;
    }

    public double getSecondWinOrTie() {
        return secondWinOrTie;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public int getBetMade() {
        return betMade;
    }

    public void setBetMade(int betMade) {
        this.betMade = betMade;
    }

    public String getLink() {
        return link;
    }

    public RuleNumber getRuleNumber() {
        return ruleNumber;
    }

    public void setRuleNumber(RuleNumber ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(league, game.league) &&
                Objects.equals(leagueLink, game.leagueLink) &&
                Objects.equals(dateTime, game.dateTime) &&
                Objects.equals(firstTeam, game.firstTeam) &&
                Objects.equals(secondTeam, game.secondTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(league, leagueLink, dateTime, firstTeam, secondTeam);
    }

    @Override
    public String toString() {
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        return DATE_FORMATTER.format(dateTime) +
                " \"" + league +
                "\" " + firstTeam +
                " - " + secondTeam +
                (betMade == 1
                        ? " (BET MADE) "
                        : " (BET NOT MADE) ") +
                (gameResult == GameResult.NO_RESULT
                        ? ""
                        : " (" + gameResult + ")");
    }
}
