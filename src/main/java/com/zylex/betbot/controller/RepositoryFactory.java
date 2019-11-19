package com.zylex.betbot.controller;

import com.zylex.betbot.service.Day;
import com.zylex.betbot.service.bet.rule.RuleNumber;

public class RepositoryFactory {

    private Repository todayRepository;

    private Repository tomorrowRepository;

    public RepositoryFactory(RuleNumber ruleNumber) {
        todayRepository = new Repository(Day.TODAY, ruleNumber);
        tomorrowRepository = new Repository(Day.TOMORROW, ruleNumber);
    }

    public Repository getRepository(Day day) {
        if (day == Day.TODAY) {
            return todayRepository;
        } else if (day == Day.TOMORROW) {
            return tomorrowRepository;
        }
        return null;
    }
}
