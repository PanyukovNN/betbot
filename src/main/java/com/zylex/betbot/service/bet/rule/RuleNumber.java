package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    RULE_ONE(BetCoefficient.FIRST_WIN, 0.1d),
    RULE_TWO(BetCoefficient.ONE_X, 0.02d);

    public final BetCoefficient betCoefficient;

    public final double PERCENT;

    RuleNumber(BetCoefficient betCoefficient, double PERCENT) {
        this.betCoefficient = betCoefficient;
        this.PERCENT = PERCENT;
    }
}
