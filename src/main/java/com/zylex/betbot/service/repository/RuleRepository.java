package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.rule.Rule;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RuleRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public RuleRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Rule> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Rule");
        try {
            return ((List<Rule>) query.getResultList()).stream()
                    .sorted(Comparator.comparing(Rule::getId))
                    .collect(Collectors.toList());
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Rule> getByNames(List<String> ruleNames) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Rule WHERE name in :name");
        query.setParameter("name", ruleNames);
        try {
            return ((List<Rule>) query.getResultList()).stream()
                    .sorted(Comparator.comparing(Rule::getId))
                    .collect(Collectors.toList());
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
