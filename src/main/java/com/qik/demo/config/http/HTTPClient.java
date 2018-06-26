package com.qik.demo.config.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * HTTP 访问包装类
 * <p>
 * 包装了常用的 get 和 post 方法，并提供了返回原始http body string的快捷方法。
 *
 * @author liyanliang
 */
public class HTTPClient implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(HTTPClient.class);

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;
    private final RequestConfig config;

    HTTPClient(CloseableHttpClient httpClient, RequestConfig requestConfig) {
        this.httpClient = httpClient;
        this.config = requestConfig;
    }

    @NotNull
    private static HttpGet prepareGet(@NotNull final String url, @NotNull final RequestConfig config) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        return httpGet;
    }

    private static List<NameValuePair> convertParams(@Nullable Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>();
        params = Optional.ofNullable(params).orElse(Collections.emptyMap());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return pairs;
    }

    @NotNull
    private static HttpPost preparePost(@NotNull final String url, @NotNull final HttpEntity entity, @NotNull final RequestConfig config) {
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        post.setConfig(config);
        return post;
    }

    /**
     * Convert response entity to http response body string.
     *
     * @param entity http response tntity
     * @return body string
     * @throws IOException any I/O exceptions
     */
    public static String toString(HttpEntity entity) throws IOException {
        return EntityUtils.toString(entity, DEFAULT_CHARSET);
    }

    private HttpGet prepareGet(@NotNull String url) {
        logger.info("HTTP get: " + url);
        return prepareGet(url, config);
    }

    /**
     * HTTP GET
     *
     * @param url target url
     * @return response body string, {@code null} if any exception occurs..
     */
    public String getString(@NotNull String url) {
        logger.info("HTTP get: " + url);

        HttpGet httpGet = prepareGet(url, config);
        try {
            return executeForString(httpGet);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * HTTP GET
     *
     * @param url    target url
     * @param tClass type class
     * @param <T>    response type
     * @return response body
     */
    public <T> T get(@NotNull String url, Class<T> tClass) throws IOException {
        HttpGet httpGet = prepareGet(url);
        return executeForType(httpGet, tClass);
    }

    /**
     * HTTP GET
     *
     * @param url    target url
     * @param tClass type class
     * @param <T>    response type
     * @return response body
     */
    public <T> T get(@NotNull String url, TypeReference<T> tClass) throws IOException {
        HttpGet httpGet = prepareGet(url);
        return executeForType(httpGet, tClass);
    }

    private HttpPost preparePost(@NotNull String url, @NotNull List<NameValuePair> params) {
        logger.info("HTTP post: [" + url + "] params: [" + params + "]");

        HttpEntity entity = EntityBuilder.create()
                .setParameters(params)
                .build();

        return preparePost(url, entity, config);
    }

    private HttpPost preparePost(@NotNull String url, @NotNull String content) throws UnsupportedEncodingException {
        logger.info("HTTP post: [" + url + "] content: [" + content + "]");

        HttpEntity entity = EntityBuilder.create()
                .setContentType(ContentType.APPLICATION_JSON)
                .setText(content)
                .build();

        HttpPost post = preparePost(url, entity, config);
        post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        return post;
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @return response entity
     * @throws IOException any I/O exception
     */
    public <T> T post(@NotNull String url, @Nullable Map<String, String> params, Class<T> tClass) throws IOException {
        List<NameValuePair> pairs = convertParams(params);
        return post(url, pairs, tClass);
    }

    public <T> T post(@NotNull String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers, Class<T> tClass) throws IOException {
        List<NameValuePair> pairs = convertParams(params);
        List<NameValuePair> headerPairs = convertParams(headers);
        return post(url, pairs, headerPairs, tClass);
    }

    public <T> T post(@NotNull String url, @NotNull List<NameValuePair> params, @Nullable List<NameValuePair> headers, Class<T> tClass) throws IOException {
        HttpPost post = preparePost(url, params);
        for (NameValuePair nameValuePair : headers) {
            post.addHeader(nameValuePair.getName(), nameValuePair.getValue());
        }
        return executeForType(post, tClass);
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @return response entity
     * @throws IOException any I/O exception
     */
    public <T> T post(@NotNull String url, @Nullable Map<String, String> params, TypeReference<T> type) throws IOException {
        List<NameValuePair> pairs = convertParams(params);
        return post(url, pairs, type);
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @return response entity
     * @throws IOException any I/O exception
     */
    public <T> T post(@NotNull String url, @NotNull List<NameValuePair> params, Class<T> tClass) throws IOException {
        HttpPost post = preparePost(url, params);
        return executeForType(post, tClass);
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @param type   type reference
     * @return response entity
     * @throws IOException any I/O exception
     */
    public <T> T post(@NotNull String url, @NotNull List<NameValuePair> params, TypeReference<T> type) throws IOException {
        HttpPost post = preparePost(url, params);
        return executeForType(post, type);
    }

    /**
     * HTTP POST
     *
     * @param url     target url
     * @param content post params
     * @return response body string, {@code null} if any exception occurs..
     */
    public <T> T postString(@NotNull String url, @NotNull Object content, TypeReference<T> type) {
        String _content;
        try {
            _content = objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        try {
            HttpPost post = preparePost(url, _content);
            return executeForType(post, type);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @return response body string, {@code null} if any exception occurs..
     */
    public String postString(@NotNull String url, @NotNull Map<String, String> params) {
        List<NameValuePair> pairs = convertParams(params);
        return postString(url, pairs);
    }

    /**
     * HTTP POST
     *
     * @param url    target url
     * @param params post params
     * @return response body string, {@code null} if any exception occurs..
     */
    public String postString(@NotNull String url, @NotNull List<NameValuePair> params) {
        HttpPost post = preparePost(url, params);
        try {
            return executeForString(post);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 内容 post 到指定地址
     *
     * @param url     要访问的地址
     * @param content JSON 格式数据
     * @return 服务器返回值
     */
    public String postString(@NotNull String url, @NotNull String content) {
        try {
            HttpPost post = preparePost(url, content);
            return executeForString(post);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public <T> T post(@NotNull String url, String params, @NotNull Map<String, String> header, Class<T> tClass) throws IOException {
        logger.info("HTTP post: [" + url + "] content: [" + params + "]");
        HttpPost post = new HttpPost(url);
        Iterator<Map.Entry<String, String>> iterator = header.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            post.setHeader(next.getKey(), next.getValue());
        }

        HttpEntity entity = EntityBuilder.create()
                .setText(params)
                .build();
        post.setEntity(entity);
        return executeForType(post, tClass);
    }

    private String executeForString(final HttpUriRequest request) throws IOException {
        logger.info("Before request :" + request.getURI() + ", " + request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            logger.info("HTTP request success: " + entity + ", body-length:" + entity.getContentLength());
            String body = toString(entity);
            if (logger.isDebugEnabled()) {
                logger.debug("Request response: [" + body + "]");
            }
            return body;
        }
    }

    private <T> T executeForType(final HttpUriRequest request, Class<T> clazz) throws IOException {
        return executeForType(request, objectMapper.getTypeFactory().constructType(clazz));
    }

    private <T> T executeForType(final HttpUriRequest request, TypeReference<T> reference) throws IOException {
        return executeForType(request, objectMapper.getTypeFactory().constructType(reference));
    }

    private <T> T executeForType(final HttpUriRequest request, JavaType valueType) throws IOException {
        logger.info("Before request :" + request.getURI() + ", " + request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            logger.info("HTTP request success: " + entity + ", body-length:" + entity.getContentLength());

            InputStream inputStream = entity.getContent();
            T t = objectMapper.readValue(inputStream, valueType);
            if (logger.isDebugEnabled()) {
                logger.debug("Request response: [" + t + "]");
            }
            return t;
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }
}
