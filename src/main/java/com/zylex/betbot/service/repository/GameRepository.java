package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.rule.RuleNumber;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class GameRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public GameRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Game save(Game game) {
        Session session = sessionFactory.getCurrentSession();
        Game retreatedGame = getById(game.getId());
        if (retreatedGame.getLink() == null) {
            Long id = (Long) session.save(game);
            game.setId(id);
            return game;
        } else {
            return retreatedGame;
        }
    }

    public Game getById(long id) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Game WHERE id = :gameId");
        query.setParameter("gameId", id);
        try {
            return (Game) query.getSingleResult();
        } catch (NoResultException e) {
            return new Game();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Game> getByDate(RuleNumber ruleNumber, LocalDate date) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Game WHERE dateTime >= :dayStart AND dateTime <= :dayEnd");
        query.setParameter("dayStart", LocalDateTime.of(date, LocalTime.MIN));
        query.setParameter("dayEnd", LocalDateTime.of(date, LocalTime.MAX));
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public synchronized void update(Game game) {
        Session session = sessionFactory.getCurrentSession();
        session.update(game);
    }

    public void delete(Game game) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(game);
    }

    @SuppressWarnings("unchecked")
    public List<Game> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Game");
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
