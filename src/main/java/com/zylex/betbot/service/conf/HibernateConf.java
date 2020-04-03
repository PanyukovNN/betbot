package com.zylex.betbot.service.conf;

import com.zylex.betbot.BetBotApplication;
import com.zylex.betbot.exception.HibernateConfException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateConf {

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.zylex.betbot.model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try(InputStream inputStream = BetBotApplication.class.getClassLoader().getResourceAsStream("BetBotDb.properties")) {
            Properties property = new Properties();
            property.load(inputStream);
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(property.getProperty("db.url"));
            dataSource.setUsername(property.getProperty("db.login"));
            dataSource.setPassword(property.getProperty("db.password"));
            return dataSource;
        } catch(IOException e) {
            throw new HibernateConfException(e.getMessage(), e);
        }
    }

    @Bean
    public PlatformTransactionManager hibernateTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        return hibernateProperties;
    }
}