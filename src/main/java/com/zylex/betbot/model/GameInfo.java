package com.zylex.betbot.model;

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

    @Column(name = "first_win_or_tie")
    private double firstWinOrTie;

    @Column(name = "second_win_or_tie")
    private double secondWinOrTie;

    public GameInfo() {
    }

    public GameInfo(double firstWin, double tie, double secondWin, double firstWinOrTie, double secondWinOrTie) {
        this.firstWin = firstWin;
        this.tie = tie;
        this.secondWin = secondWin;
        this.firstWinOrTie = firstWinOrTie;
        this.secondWinOrTie = secondWinOrTie;
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

    public double getFirstWinOrTie() {
        return firstWinOrTie;
    }

    public void setFirstWinOrTie(double firstWinOrTie) {
        this.firstWinOrTie = firstWinOrTie;
    }

    public double getSecondWinOrTie() {
        return secondWinOrTie;
    }

    public void setSecondWinOrTie(double secondWinOrTie) {
        this.secondWinOrTie = secondWinOrTie;
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
                firstWinOrTie,
                secondWinOrTie);
    }
}
