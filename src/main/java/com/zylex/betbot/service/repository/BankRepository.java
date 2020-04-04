package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.Bank;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BankRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public BankRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Bank save(Bank bank) {
        Session session = sessionFactory.getCurrentSession();
        Long id = (Long) session.save(bank);
        bank.setId(id);
        return bank;
    }
}
