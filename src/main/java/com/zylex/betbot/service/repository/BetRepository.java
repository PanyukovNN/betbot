package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.bet.Bet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public class BetRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public BetRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Bet save(Bet bet) {
        Session session = sessionFactory.getCurrentSession();
        Long id = (Long) session.save(bet);
        bet.setId(id);
        session.getTransaction().commit();
        session.beginTransaction();
        return bet;
    }
}
