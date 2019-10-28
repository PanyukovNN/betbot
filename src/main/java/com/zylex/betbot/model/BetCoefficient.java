package com.zylex.betbot.model;

public enum BetCoefficient {
    FIRST_WIN(0),
    TIE(1),
    SECOND_WIN(2),
    FIRST_WIN_OR_TIE(3),
    SECOND_WIN_OR_TIE(5);

    public final int index;

    private BetCoefficient(int index) {
        this.index = index;
    }
}
