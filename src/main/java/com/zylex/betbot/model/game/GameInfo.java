package com.zylex.betbot.model.game;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "game_info")
public class GameInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "first_win")
    private double firstWin;

    @Column(name = "tie")
    private double tie;

    @Column(name = "second_win")
    private double secondWin;

    @Column(name = "one_x")
    private double oneX;

    @Column(name = "x_two")
    private double xTwo;

    public GameInfo() {
    }

    public GameInfo(double firstWin, double tie, double secondWin, double oneX, double xTwo) {
        this.firstWin = firstWin;
        this.tie = tie;
        this.secondWin = secondWin;
        this.oneX = oneX;
        this.xTwo = xTwo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public double getFirstWin() {
        return firstWin;
    }

    public void setFirstWin(double firstWin) {
        this.firstWin = firstWin;
    }

    public double getTie() {
        return tie;
    }

    public void setTie(double tie) {
        this.tie = tie;
    }

    public double getSecondWin() {
        return secondWin;
    }

    public void setSecondWin(double secondWin) {
        this.secondWin = secondWin;
    }

    public double getOneX() {
        return oneX;
    }

    public void setOneX(double oneX) {
        this.oneX = oneX;
    }

    public double getXTwo() {
        return xTwo;
    }

    public void setXTwo(double xTwo) {
        this.xTwo = xTwo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameInfo gameInfo = (GameInfo) o;
        return id == gameInfo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String body = "%.2f|%.2f|%.2f %.2f|%.2f";
        return String.format(body,
                firstWin,
                tie,
                secondWin,
                oneX,
                xTwo);
    }
}
