package com.zylex.betbot.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Instance of a football game.
 */
@Entity
@Table(name = "game")
public class Game implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "first_team")
    private String firstTeam;

    @Column(name = "second_team")
    private String secondTeam;

    @ManyToMany
    @JoinTable(
            name = "game_rule",
            joinColumns = { @JoinColumn(name = "game_id") },
            inverseJoinColumns = { @JoinColumn(name = "rule_id") }
    )
    private List<Rule> rules = new ArrayList<>();

    @Column(name = "result")
    private String result;

    @Column(name = "bet_made")
    private boolean betMade;

    @Column(name = "link")
    private String link;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private GameInfo gameInfo;

    public Game() {
    }

    public Game(LocalDateTime dateTime, League league, String firstTeam, String secondTeam, String result, boolean betMade, String link, GameInfo gameInfo) {
        this.dateTime = dateTime;
        this.league = league;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.result = result;
        this.betMade = betMade;
        this.link = link;
        this.gameInfo = gameInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public String getFirstTeam() {
        return firstTeam;
    }

    public void setFirstTeam(String firstTeam) {
        this.firstTeam = firstTeam;
    }

    public String getSecondTeam() {
        return secondTeam;
    }

    public void setSecondTeam(String secondTeam) {
        this.secondTeam = secondTeam;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isBetMade() {
        return betMade;
    }

    public void setBetMade(boolean betMade) {
        this.betMade = betMade;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game gameTemp = (Game) o;
        return id == gameTemp.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        String body = "%d %s %s '%s'-vs-'%s' (%s) (%s) (%s) %s";
        return String.format(body,
                id,
                DATE_TIME_FORMATTER.format(dateTime),
                league,
                firstTeam,
                secondTeam,
                rules,
                result,
                (betMade ? "BET MADE"
                        : "BET NOT MADE"),
                gameInfo);
    }
}
