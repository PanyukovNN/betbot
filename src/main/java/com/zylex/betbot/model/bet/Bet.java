package com.zylex.betbot.model.bet;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "bet")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @Column(name = "status")
    private String status;

    @Column(name = "amount")
    private int amount;

    @Column(name = "coefficient")
    private String coefficient;

    public Bet() {
    }

    public Bet(LocalDateTime dateTime, Game game, Rule rule, String status, int amount, String coefficient) {
        this.dateTime = dateTime;
        this.game = game;
        this.rule = rule;
        this.status = status;
        this.amount = amount;
        this.coefficient = coefficient;
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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bet that = (Bet) o;
        return Objects.equals(game, that.game) &&
                Objects.equals(rule, that.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, rule);
    }

    @Override
    public String toString() {
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM");
        return String.format("%s %d rub. placed on %s (%s)",
                DATE_TIME_FORMATTER.format(dateTime),
                amount,
                coefficient,
                status);
    }
}
