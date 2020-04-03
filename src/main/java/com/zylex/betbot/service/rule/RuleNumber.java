package com.zylex.betbot.service.rule;

import com.zylex.betbot.model.BetCoefficient;

/**
 * Specifies rule.
 */
public enum RuleNumber {
    FIRST_WIN(BetCoefficient.FIRST_WIN, 0.05d, new FirstRuleFilter()),
    X_TWO(BetCoefficient.X_TWO, 0.05d, new XTwoRuleFilter());

    public final BetCoefficient betCoefficient;

    public final double percent;

    public final RuleFilter ruleFilter;

    RuleNumber(BetCoefficient betCoefficient, double percent, RuleFilter ruleFilter) {
        this.betCoefficient = betCoefficient;
        this.percent = percent;
        this.ruleFilter = ruleFilter;
    }

}
