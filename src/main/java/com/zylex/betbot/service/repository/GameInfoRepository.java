package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.model.GameInfo;
import com.zylex.betbot.service.rule.RuleNumber;
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

    @SuppressWarnings("unchecked")
    public List<Game> getByRuleAndDate(RuleNumber ruleNumber, LocalDate date) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM GameTemp WHERE rule = :rule AND dateTime >= :dayStart AND dateTime <= :dayEnd");
        query.setParameter("rule", ruleNumber.toString());
        query.setParameter("dayStart", LocalDateTime.of(date, LocalTime.MIN));
        query.setParameter("dayEnd", LocalDateTime.of(date, LocalTime.MAX));
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
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
