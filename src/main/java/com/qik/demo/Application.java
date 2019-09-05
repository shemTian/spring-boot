package com.qik.demo;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public RestTemplate getRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
    for (HttpMessageConverter converter : converters) {
      if (converter instanceof StringHttpMessageConverter) {
        ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
        break;
      }
    }
    return restTemplate;
  }
}
