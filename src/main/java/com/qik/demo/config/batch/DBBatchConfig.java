package com.qik.demo.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.Clock;


/**
 * InMemoryBatchConfig
 * 数据库调度任务，可中断，可重启（从原任务执行结束点启动）
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 15:36
 **/
@EnableBatchProcessing
@Configuration
@Slf4j
public class DBBatchConfig {


    /**
     * 任务执行状态会插入数据库，重启容器可恢复
     *
     * @param jobRepository
     * @return
     */
    @Bean(name = "dbJobLau")
    public SimpleJobLauncher dbJobLauncher(@Qualifier("jobRepository") JobRepository jobRepository
            , @Qualifier("appThreadPool") TaskExecutor taskExecutor) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        return jobLauncher;
    }

    /**
     * job监控，可通过此bean查询遍历当前运行的job，并进行一系列操作
     * @param dbJobLauncher
     * @param jobRepository
     * @param jobRegistry
     * @param jobExplorer
     * @return
     */
    @Bean(name = "dbJobOperator")
    public JobOperator dbJobOperator(@Qualifier("dbJobLau") JobLauncher dbJobLauncher
            , @Qualifier("dbJobRep") JobRepository jobRepository
            , JobRegistry jobRegistry
            , JobExplorer jobExplorer) {
        SimpleJobOperator simpleJobOperator = new SimpleJobOperator();
        simpleJobOperator.setJobLauncher(dbJobLauncher);
        simpleJobOperator.setJobRepository(jobRepository);
        simpleJobOperator.setJobExplorer(jobExplorer);
        simpleJobOperator.setJobRegistry(jobRegistry);
        return simpleJobOperator;
    }
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
    @Bean(name = "dbJobRep")
    public JobRepository dbJobRepository(PlatformTransactionManager transactionManager,
                                         @Qualifier("dataSource") DataSource dataSource) throws Exception {
        JobRepositoryFactoryBean dbJobFactory = new JobRepositoryFactoryBean();
        dbJobFactory.setTransactionManager(transactionManager);
        dbJobFactory.setDataSource(dataSource);
        return dbJobFactory.getObject();
    }
}
