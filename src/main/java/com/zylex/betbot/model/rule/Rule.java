package com.zylex.betbot.model.rule;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "rule")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "selected_leagues")
    private boolean selectedLeagues;

    @Column(name = "percent")
    private double percent;

    @Column(name = "bet_coefficient")
    private String betCoefficient;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_id")
    private List<RuleCondition> ruleConditions = new ArrayList<>();

    @Column(name = "activate")
    private boolean activate;

    public Rule() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelectedLeagues() {
        return selectedLeagues;
    }

    public void setSelectedLeagues(boolean selectedLeagues) {
        this.selectedLeagues = selectedLeagues;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public String getBetCoefficient() {
        return betCoefficient;
    }

    public void setBetCoefficient(String betCoefficient) {
        this.betCoefficient = betCoefficient;
    }

    public List<RuleCondition> getRuleConditions() {
        return ruleConditions;
    }

    public void setRuleConditions(List<RuleCondition> ruleConditions) {
        this.ruleConditions = ruleConditions;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(name, rule.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
