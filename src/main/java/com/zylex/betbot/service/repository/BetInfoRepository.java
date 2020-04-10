package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.bet.BetInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class BetInfoRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public BetInfoRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public BetInfo save(BetInfo betInfo) {
        Session session = sessionFactory.getCurrentSession();
        betInfo.setId(1);
        session.update(betInfo);
        return betInfo;
    }

    public BetInfo getLast() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM BetInfo ORDER BY id DESC");
        query.setMaxResults(1);
        try {
            return (BetInfo) query.uniqueResult();
        } catch (NoResultException e) {
            return new BetInfo();
        }
    }
}
