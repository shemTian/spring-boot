package com.qik.demo.config.web;

import com.qik.demo.config.filter.RequestWrapperFilter;
import com.qik.demo.config.web.msgconvert.AccountArgumentResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;
import java.util.List;

/**
 * WebConfig
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/19 11:33
 **/
@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter {

    @Bean
    public Filter requestWrapper() {
        return new RequestWrapperFilter();
    }
    @Bean
    public FilterRegistrationBean crossFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestWrapper());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public AccountArgumentResolver accountArgumentResolver() {
        return new AccountArgumentResolver();
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(accountArgumentResolver());
    }
}
