package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Rule;
import com.zylex.betbot.service.rule.RuleNumber;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class RuleRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public RuleRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Rule getByRuleNumber(RuleNumber ruleNumber) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Rule WHERE name = :ruleNumber");
        query.setParameter("ruleNumber", ruleNumber.toString());
        try {
            return (Rule) query.getSingleResult();
        } catch (NoResultException e) {
            return new Rule();
        }
    }
}
