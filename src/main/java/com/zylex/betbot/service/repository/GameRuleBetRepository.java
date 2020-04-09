package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.GameRuleBet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;

@Repository
public class GameRuleBetRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public GameRuleBetRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public GameRuleBet save(GameRuleBet gameRuleBet) {
        Session session = sessionFactory.getCurrentSession();
        GameRuleBet retreatedGameRuleBet = get(gameRuleBet);
        if (retreatedGameRuleBet.getId() == 0) {
            Long id = (Long) session.save(gameRuleBet);
            gameRuleBet.setId(id);
            return gameRuleBet;
        } else {
            return retreatedGameRuleBet;
        }
    }

    public GameRuleBet get(GameRuleBet gameRuleBet) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM GameRuleBet WHERE id = :gameRuleBetId");
        query.setParameter("gameRuleBetId", gameRuleBet.getId());
        try {
            return (GameRuleBet) query.getSingleResult();
        } catch (NoResultException e) {
            return new GameRuleBet();
        }
    }
}
