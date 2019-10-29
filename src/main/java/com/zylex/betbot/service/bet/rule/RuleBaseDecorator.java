package com.zylex.betbot.service.bet.rule;

import com.zylex.betbot.model.EligibleGameContainer;
import com.zylex.betbot.model.Game;

import java.util.List;

public class RuleBaseDecorator implements Rule {

    private Rule rule;

    public RuleBaseDecorator(Rule rule) {
        this.rule = rule;
    }

    @Override
    public EligibleGameContainer filter(List<Game> games) {
        return rule.filter(games);
    }
}
