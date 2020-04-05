package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Rule;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

@Repository
public class RuleRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public RuleRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    public List<Rule> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Rule");
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
