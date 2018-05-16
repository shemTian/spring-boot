package com.qik.demo.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ThreadPool
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 15:39
 **/
@Configuration
public class ThreadPool {

    @Bean(name = "appThreadPool")
    public ThreadPoolTaskExecutor springExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(2000);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("app-thread-");
        return executor;
    }
}
