package com.zylex.betbot.model;

public enum BetCoefficient {
    FIRST_WIN(0),
    TIE(1),
    SECOND_WIN(2),
    ONE_X(3),
    X_TWO(5);

    public final int INDEX;

    private BetCoefficient(int INDEX) {
        this.INDEX = INDEX;
    }
}
