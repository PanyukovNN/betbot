package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.game.Game;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.zylex.betbot.BetBotApplication.BET_START_TIME;
import static com.zylex.betbot.BetBotApplication.BOT_START_TIME;

@Repository
public class GameRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public GameRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Game save(Game game) {
        Session session = sessionFactory.getCurrentSession();
        Game retreatedGame = get(game);
        if (retreatedGame.getLink() == null) {
            Long id = (Long) session.save(game);
            game.setId(id);
            return game;
        } else {
            return retreatedGame;
        }
    }

    public Game get(Game game) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Game WHERE link = :gameLink");
        query.setParameter("gameLink", game.getLink());
        try {
            return (Game) query.getSingleResult();
        } catch (NoResultException e) {
            return new Game();
        }
    }

    @Transactional
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

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Game> getByDate(LocalDate date) {
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

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Game> getSinceDateTime(LocalDateTime startDateTime) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Game WHERE dateTime >= :startDateTime");
        query.setParameter("startDateTime", startDateTime);
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public List<Game> getByBetStartTime() {
        return getSinceDateTime(LocalDateTime.of(BOT_START_TIME.toLocalDate().minusDays(1), BET_START_TIME));
    }

    public void update(Game game) {
        Session session = sessionFactory.getCurrentSession();
        session.update(game);
        session.getTransaction().commit();
        session.beginTransaction();
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
