package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.game.GameInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Repository
public class GameInfoRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public GameInfoRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public GameInfo save(GameInfo gameInfo) {
        Session session = sessionFactory.getCurrentSession();
        GameInfo retreatedGameInfo = get(gameInfo);
        if (retreatedGameInfo.getId() == 0) {
            Long id = (Long) session.save(gameInfo);
            gameInfo.setId(id);
            return gameInfo;
        } else {
            return retreatedGameInfo;
        }
    }

    public GameInfo get(GameInfo gameInfo) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM GameInfo WHERE id = :gameInfoId");
        query.setParameter("gameInfoId", gameInfo.getId());
        try {
            return (GameInfo) query.getSingleResult();
        } catch (NoResultException e) {
            return new GameInfo();
        }
    }

    public GameInfo getByGame(Game game) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM GameInfo WHERE game = :game");
        query.setParameter("game", game);
        try {
            return (GameInfo) query.getSingleResult();
        } catch (NoResultException e) {
            return new GameInfo();
        }
    }

    public synchronized void update(GameInfo gameInfo) {
        Session session = sessionFactory.getCurrentSession();
        session.update(gameInfo);
    }

    @SuppressWarnings("unchecked")
    public List<GameInfo> getAll() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM GameInfo");
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
