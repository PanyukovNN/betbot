package com.zylex.betbot.model;

import java.time.LocalDateTime;

public class Game {

    private String league;

    private LocalDateTime dateTime;

    private String firstTeam;

    private String secondTeam;

    private String firstWin;

    private String tie;

    private String secondWin;

    private String firstWinOrTie;

    private String secondWinOrTie;

    private String leagueLink;

    public Game(String league, LocalDateTime dateTime, String firstTeam, String secondTeam, String firstWin, String tie, String secondWin, String firstWinOrTie, String secondWinOrTie, String leagueLink) {
        this.league = league;
        this.dateTime = dateTime;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.firstWin = firstWin;
        this.tie = tie;
        this.secondWin = secondWin;
        this.firstWinOrTie = firstWinOrTie;
        this.secondWinOrTie = secondWinOrTie;
        this.leagueLink = leagueLink;
    }

    public String getLeague() {
        return league;
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

    public String getFirstWin() {
        return firstWin;
    }

    public String getTie() {
        return tie;
    }

    public String getSecondWin() {
        return secondWin;
    }

    public String getFirstWinOrTie() {
        return firstWinOrTie;
    }

    public String getSecondWinOrTie() {
        return secondWinOrTie;
    }

    public String getLeagueLink() {
        return leagueLink;
    }

    @Override
    public String toString() {
        return "Game{" +
                "league='" + league + '\'' +
                ", dateTime=" + dateTime +
                ", firstTeam='" + firstTeam + '\'' +
                ", secondTeam='" + secondTeam + '\'' +
                ", firstWin='" + firstWin + '\'' +
                ", tie='" + tie + '\'' +
                ", secondWin='" + secondWin + '\'' +
                ", firstWinOrTie='" + firstWinOrTie + '\'' +
                ", secondWinOrTie='" + secondWinOrTie + '\'' +
                '}';
    }
}
