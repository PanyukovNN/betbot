package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Bet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;

@Repository
public class BetRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public BetRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Bet save(Bet bet) {
        Session session = sessionFactory.getCurrentSession();
        Bet retreatedBet = get(bet);
        if (retreatedBet.getRule() == null) {
            Long id = (Long) session.save(bet);
            bet.setId(id);
            return bet;
        } else {
            return retreatedBet;
        }
    }

    public Bet get(Bet bet) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Bet WHERE id = :betId");
        query.setParameter("betId", bet.getId());
        try {
            return (Bet) query.getSingleResult();
        } catch (NoResultException e) {
            return new Bet();
        }
    }
}
