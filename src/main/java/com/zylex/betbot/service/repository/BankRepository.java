package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Bank;
import com.zylex.betbot.model.BetInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

@Repository
public class BankRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public BankRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Bank save(Bank bank) {
        Session session = sessionFactory.getCurrentSession();
        Bank retreatedBank = getById(bank);
        if (retreatedBank.getDate() == null) {
            Long id = (Long) session.save(bank);
            bank.setId(id);
            return bank;
        } else {
            return retreatedBank;
        }
    }

    public Bank getById(Bank bank) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Bank WHERE id = :bankId");
        query.setParameter("bankId", bank.getId());
        try {
            return (Bank) query.getSingleResult();
        } catch (NoResultException e) {
            return new Bank();
        }
    }

    public Bank getLast() {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Bank ORDER BY id DESC");
        query.setMaxResults(1);
        try {
            return (Bank) query.uniqueResult();
        } catch (NoResultException e) {
            return new Bank();
        }
    }
}
