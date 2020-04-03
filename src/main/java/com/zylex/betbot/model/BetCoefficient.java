package com.zylex.betbot.model;

/**
 * Specifies the coefficient for betting.
 */
@SuppressWarnings("unused")
public enum BetCoefficient {
    NULL(-1),
    FIRST_WIN(0),
    TIE(1),
    SECOND_WIN(2),
    ONE_X(3),
    X_TWO(5);

    public final int INDEX;

    BetCoefficient(int INDEX) {
        this.INDEX = INDEX;
    }
}
