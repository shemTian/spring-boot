package com.qik.demo.config.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * JobConfig
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/6/19 16:43
 **/
@Configuration
public class JobConfig {
    /**
     * 创建job，不通过springBean管理,否则不能自定义jobName,不经过容器管理，不能使用jobExplorer处理批量进程
     *
     * @param jobBuilderFactory
     * @param step
     * @param listener
     * @param jobRepository
     * @param jobName
     * @return
     */
    public static final String FIRST_JOB = "firstJob";

    @Bean(name = "firstJob")
    public Job tagJob(JobBuilderFactory jobBuilderFactory
            , Step step
            , JobExecutionListener listener
            , JobRepository jobRepository) {
        return jobBuilderFactory.get(FIRST_JOB)
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    @Bean(name = "firstJob_firstStep")
    public Step step(StepBuilderFactory stepBuilderFactory
            ,@Qualifier("reader") ItemReader<Object> itemReader
            , ItemWriter<Object> itemWriter
            , ItemProcessor<Object, Object> processor
            , @Qualifier("dbJobRep") JobRepository memoryRepository
            , @Qualifier("appThreadPool") TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("firstStep")
                .repository(memoryRepository)
                //1000 个item处理一次
                .<Object, Object>chunk(1000)
                .reader(itemReader) //待完善
                .processor(processor)  //待完善
                .writer(itemWriter) //待完善
                .taskExecutor(taskExecutor) //多线程处理
//                .throttleLimit(100)          //最多开启线程数,默认为4
                .build();
    }

}
