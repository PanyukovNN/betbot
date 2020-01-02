package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.League;
import com.zylex.betbot.service.rule.RuleNumber;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

@Repository
public class LeagueRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public LeagueRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public League save(League league) {
        Session session = sessionFactory.getCurrentSession();
        League retreatedLeague = get(league);
        if (retreatedLeague.getName() == null) {
            Long id = (Long) session.save(league);
            league.setId(id);
            return league;
        } else {
            return retreatedLeague;
        }
    }

    public League get(League league) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM League WHERE link = :leagueLink");
        query.setParameter("leagueLink", league.getLink());
        try {
            return (League) query.getSingleResult();
        } catch (NoResultException e) {
            return new League();
        }
    }

    @SuppressWarnings("unchecked")
    public List<League> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM League");
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public League getById(long leagueId) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM League WHERE id = :leagueId");
        query.setParameter("leagueId", leagueId);
        try {
            return (League) query.getSingleResult();
        } catch (NoResultException e) {
            return new League();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getExcludeLeagues(RuleNumber ruleNumber) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createSQLQuery("SELECT league_link FROM exclude_league WHERE rule_number = :ruleNumber");
        query.setParameter("ruleNumber", ruleNumber.toString());
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllSelectedLeagues() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createSQLQuery("SELECT league_link FROM selected_league");
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}