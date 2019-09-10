package com.qik.demo.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/**
 * BatchConfig
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 15:44
 **/
@Configuration
//@EnableBatchProcessing
@Slf4j
public class BatchConfig {
    /**
     * 任务注册，不注册，不能通过{@link org.springframework.batch.core.launch.JobOperator}管理
     * @param jobRegistry
     * @return
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    /**
     * 游标查询会一次拉去所有符合条件数据,可能撑爆内存
     * @param sqlSessionFactory
     * @param queryId
     * @param {@link org.springframework.batch.core.JobParameter}传入参数
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Object> reader(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory
            , @Value("#{jobParameters[queryId]}") String queryId) {
        MyBatisCursorItemReader itemReader = new MyBatisCursorItemReader();
        itemReader.setSqlSessionFactory(sqlSessionFactory);
        itemReader.setQueryId(queryId);
        Map<String, Object> parameter = new HashMap<>();
        // TODO 传入sql查询条件
        itemReader.setParameterValues(parameter);
        return itemReader;
    }

    /**
     * mybatis 分页读取数据
     * @param sqlSessionFactory
     * @param queryId
     * @param parameter
     * @param <T>
     * @return
     */
    @Bean(name = "dbPageReader")
    @StepScope
    public <T> ItemReader<T> pageReader(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory
            , @Value("#{jobParameters[queryId]}") String queryId
            , @Value("#{jobParameters[queryMap]}") Map<String, Object> parameter) {
        MyBatisPagingItemReader itemReader = new MyBatisPagingItemReader();
        itemReader.setSqlSessionFactory(sqlSessionFactory);
        itemReader.setQueryId(queryId);
        itemReader.setParameterValues(parameter);
        itemReader.setPageSize(2000);
        return itemReader;
    }

    /**
     * mybatis批量更新操作
     * @param sqlSessionFactory
     * @param statementId
     * @param <F>
     * @return
     */
    @Bean
    @StepScope
    public ItemWriter<Object> writer(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory
            , @Value("#{jobParameters[statementId]}") String statementId) {
        MyBatisBatchItemWriter itemWriter = new MyBatisBatchItemWriter();
        itemWriter.setSqlSessionFactory(sqlSessionFactory);
        itemWriter.setStatementId(statementId);
        return itemWriter;
    }

    @Bean
    public ItemProcessor<Object, Object> processor() {
        return writerObject -> {
            // TODO reader到writer转换
            return writerObject;
        };
    }
    /**
     * 计时
     * @return
     */
    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            private Long startTime;
            private Long endTime;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                logger.info("job 开始");
                startTime = Clock.systemUTC().millis();
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                endTime = Clock.systemUTC().millis();
                if (startTime == null) {
                    logger.warn("任务执行出错");
                } else {
                    logger.info("job 结束,耗时：" + (endTime - startTime));
                }
            }
        };
    }
}
