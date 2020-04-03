package com.zylex.betbot.service.parsing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfiguration {

//    @Value("${threadpool.corepoolsize}")
//    int corePoolSize;
//
//    @Value("${threadpool.maxpoolsize}")
//    int maxPoolSize;

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(8);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }
}