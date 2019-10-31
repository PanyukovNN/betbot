package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    ONE(BetCoefficient.FIRST_WIN),
    TWO(BetCoefficient.ONE_X);

    public final BetCoefficient betCoefficient;

    RuleNumber(BetCoefficient betCoefficient) {
        this.betCoefficient = betCoefficient;
    }
}
