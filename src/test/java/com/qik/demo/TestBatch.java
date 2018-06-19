package com.qik.demo;

import com.qik.demo.config.batch.job.JobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
public class TestBatch<T, F> {

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
    @Qualifier("firstJob_firstStep")
    Step step;
    @Autowired
    @Qualifier("firstJob")
    Job job;

    /**
     * 推送客群信息
     *
     * @param groupParam
     */
    public void start(Object groupParam) {
        taskExecutor.execute(() -> {
            JobParameters jobParameters = getJobParameters(groupParam);
            try {
                dbJobLauncher.run(job, jobParameters);
            } catch (Exception e) {
                logger.error("批量查询客群发送ONS出错", e);
            }
        });
    }

    /**
     * 停止批量消息
     *
     * @param groupParam
     */
    public void stop(Object groupParam) throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
        Optional<Long> matchJobExecutionId = getExecutionId(groupParam);
        if (matchJobExecutionId.isPresent()) {
            jobOperator.stop(matchJobExecutionId.get());
        }
    }

    /**
     * 重启批量消息，从最近一次停止位置（batch_step_execution.WRITE_COUNT）开始。
     *
     * @param groupParam
     */
    public void restart(Object groupParam) throws NoSuchJobException, JobParametersInvalidException, JobRestartException, JobInstanceAlreadyCompleteException, NoSuchJobExecutionException {
        Optional<Long> matchJobExecutionId = getExecutionId(groupParam);
        if (matchJobExecutionId.isPresent()) {
            jobOperator.restart(matchJobExecutionId.get());
        }
    }

    /**
     * 获取任务执行id
     *
     * @param groupParam
     * @throws NoSuchJobException
     */
    private Optional<Long> getExecutionId(Object groupParam) throws NoSuchJobException {
        JobParameters jobParameters = getJobParameters(groupParam);
        Set<Long> runningExecutions = jobOperator.getRunningExecutions(JobConfig.FIRST_JOB);
        Optional<Long> matchJobExecutionId = runningExecutions.stream().filter(executionId -> {
            try {
                String parameters = jobOperator.getParameters(executionId);
                return jobParameters.equals(parameters);
            } catch (NoSuchJobExecutionException e) {
                logger.error("查询job出错");
            }
            return false;
        }).findFirst();
        return matchJobExecutionId;
    }

    private JobParameters getJobParameters(Object paramObj) {
        //TODO 对象属性组成jobName
        return new JobParametersBuilder()
                .addString("param", paramObj.toString())
                .toJobParameters();
    }

}
