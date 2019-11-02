package com.zylex.betbot.model;

/**
 * Specifies the coefficient for betting.
 */
@SuppressWarnings("unused")
public enum BetCoefficient {
    FIRST_WIN(0, 0.10d),
    TIE(1, 0),
    SECOND_WIN(2, 0),
    ONE_X(3, 0.02d),
    X_TWO(5, 0);

    public final int INDEX;

    public double PERCENT;

    BetCoefficient(int INDEX, double PERCENT) {
        this.INDEX = INDEX;
        this.PERCENT = PERCENT;
    }
}
