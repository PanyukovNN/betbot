package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    RULE_ONE(BetCoefficient.FIRST_WIN),
    RULE_TWO(BetCoefficient.ONE_X);

    public final BetCoefficient betCoefficient;

    RuleNumber(BetCoefficient betCoefficient) {
        this.betCoefficient = betCoefficient;
    }
}
