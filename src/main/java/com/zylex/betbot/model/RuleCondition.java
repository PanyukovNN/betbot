package com.zylex.betbot.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "rule_condition")
public class RuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "coefficient")
    private String coefficient;

    @Column(name = "operator")
    private String operator;

    @Column(name = "value")
    private double value;

    public RuleCondition() {
    }

    public RuleCondition(String coefficient, String operator, double value) {
        this.coefficient = coefficient;
        this.operator = operator;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCondition that = (RuleCondition) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RuleCondition{" +
                "id=" + id +
                ", coefficient='" + coefficient + '\'' +
                ", operator='" + operator + '\'' +
                ", value=" + value +
                '}';
    }
}
