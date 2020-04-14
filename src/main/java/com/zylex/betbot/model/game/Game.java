package com.zylex.betbot.model.game;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zylex.betbot.model.bet.Bet;
import com.zylex.betbot.model.rule.Rule;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Instance of a football game.
 */
@Entity
@Table(name = "game")
@JsonSerialize(using = GameSerializer.class)
public class Game implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "first_team")
    private String firstTeam;

    @Column(name = "second_team")
    private String secondTeam;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_rule",
            joinColumns = { @JoinColumn(name = "game_id") },
            inverseJoinColumns = { @JoinColumn(name = "rule_id") }
    )
    private Set<Rule> rules = new LinkedHashSet<>();

    @Column(name = "result")
    private String result;

    @OneToMany(mappedBy="game", fetch = FetchType.EAGER)
    private Set<Bet> bets = new HashSet<>();

    @Column(name = "link")
    private String link;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private GameInfo gameInfo;

    public Game() {
    }

    public Game(LocalDateTime dateTime, League league, String firstTeam, String secondTeam, String result, String link, GameInfo gameInfo) {
        this.dateTime = dateTime;
        this.league = league;
        this.firstTeam = firstTeam;
        this.secondTeam = secondTeam;
        this.result = result;
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

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Set<Bet> getBets() {
        return bets;
    }

    public void setBets(Set<Bet> bets) {
        this.bets = bets;
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
        Game game = (Game) o;
        return id == game.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
        return String.format("%d %s %s %s-vs-%s %s %s %s %s",
                id,
                DATE_TIME_FORMATTER.format(dateTime),
                String.format("%-15.15s", league),
                String.format("'%25.25s'", firstTeam),
                String.format("'%-25.25s'", secondTeam),
                gameInfo,
                String.format("(%s)", result),
                String.format("(Rules:%s)", rules),
                String.format("(Bets:%s)", bets));
    }
}
