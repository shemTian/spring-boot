package com.qik.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * TestBatch
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/16 16:13
 **/
@Service
@Slf4j
public class TestBatch<T,F> {

    @Autowired
    @Qualifier("appThreadPool")
    private TaskExecutor taskExecutor;


    @Autowired
    @Qualifier("dbJobLau")
    private JobLauncher dbJobLauncher;

    @Autowired
    @Qualifier("dbJobOperator")
    private JobOperator jobOperator;

    @Autowired
    JobBuilderFactory jobBuilderFactory;
    @Autowired
    @Qualifier("myStep")
    Step step;
    @Autowired
    @Qualifier("dbJobRep")
    JobRepository jobRepository;
    @Autowired
    JobExecutionListener listener;
    /**
     * 推送客群信息
     * @param groupParam
     */
    public void start(Object groupParam) {
        taskExecutor.execute(() -> {
            String jobName = groupParam.toString();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", jobName)
                    .toJobParameters();
            try {
                Job job = tagJob(jobBuilderFactory, step, listener, jobRepository, jobName);
                dbJobLauncher.run(job, jobParameters);
            } catch (Exception e) {
                logger.error("批量查询客群发送ONS出错",e);
            }
        });
    }
    /**
     * 推送客群信息
     * @param groupParam
     */
    public void stop(Object groupParam) throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
        String jobName = groupParam.toString();
        Set<Long> runningExecutions = jobOperator.getRunningExecutions(jobName);
        jobOperator.stop(runningExecutions.iterator().next());
    }

    /**
     * 创建已经job，不通过springBean管理,否则不能自定义jobName
     * @param jobBuilderFactory
     * @param step
     * @param listener
     * @param jobRepository
     * @param jobName
     * @return
     */
    public Job tagJob(JobBuilderFactory jobBuilderFactory
            , Step step
            , JobExecutionListener listener
            , JobRepository jobRepository
            , String jobName) {
        return jobBuilderFactory.get(jobName)
                .repository(jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    @Bean(name = "myStep")
    public Step step(StepBuilderFactory stepBuilderFactory
            , ItemReader<T> reader
            , ItemWriter<F> writer
            , ItemProcessor<T, F> processor
            , @Qualifier("jobRepository") JobRepository memoryRepository
            , @Qualifier("appThreadPool") TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("firstStep")
                .repository(memoryRepository)
                //1000 个item处理一次
                .<T, F>chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor) //多线程处理
//                .throttleLimit(100)          //最多开启线程数,默认为4
                .build();
    }
}
