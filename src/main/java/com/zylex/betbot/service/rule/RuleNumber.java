package com.zylex.betbot.service.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    NO_RULE(null, 0, null),
    RULE_ONE(BetCoefficient.FIRST_WIN, 0.05d, new FirstRule()),
    RULE_TEST(BetCoefficient.FIRST_WIN, 0.05d, new TestRule());

    public final BetCoefficient betCoefficient;

    public final double PERCENT;

    public final Rule rule;

    RuleNumber(BetCoefficient betCoefficient, double PERCENT, Rule rule) {
        this.betCoefficient = betCoefficient;
        this.PERCENT = PERCENT;
        this.rule = rule;
    }

}
