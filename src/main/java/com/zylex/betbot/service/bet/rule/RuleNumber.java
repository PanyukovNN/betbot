package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    RULE_ONE(BetCoefficient.FIRST_WIN, 0),
    RULE_TEST(BetCoefficient.FIRST_WIN, 0);

    public final BetCoefficient betCoefficient;

    public final double PERCENT;

    RuleNumber(BetCoefficient betCoefficient, double PERCENT) {
        this.betCoefficient = betCoefficient;
        this.PERCENT = PERCENT;
    }
}
