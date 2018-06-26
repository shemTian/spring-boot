package com.qik.demo.config.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author liyanliang
 */
public class HTTPClientFactoryBean implements FactoryBean<HTTPClient> {

    private static final Logger logger = LoggerFactory.getLogger(HTTPClientFactoryBean.class);

    private HttpHost proxy;
    private boolean authenticationEnabled = false;
    private Collection<String> targetPreferredAuthSchemes;
    private Collection<String> proxyPreferredAuthSchemes;
    private int timeout = 500;
    private boolean contentCompressionEnabled = true;

    public void setProxy(HttpHost proxy) {
        this.proxy = proxy;
    }

    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public void setTargetPreferredAuthSchemes(Collection<String> targetPreferredAuthSchemes) {
        this.targetPreferredAuthSchemes = targetPreferredAuthSchemes;
    }

    public void setProxyPreferredAuthSchemes(Collection<String> proxyPreferredAuthSchemes) {
        this.proxyPreferredAuthSchemes = proxyPreferredAuthSchemes;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setContentCompressionEnabled(boolean contentCompressionEnabled) {
        this.contentCompressionEnabled = contentCompressionEnabled;
    }

    @Override
    public HTTPClient getObject() throws Exception {
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(30, TimeUnit.MINUTES);
        // 最大连接数
        connManager.setMaxTotal(2048);
        // 默认的每个路由的最大连接数
        connManager.setDefaultMaxPerRoute(512);

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoReuseAddress(true) // 是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
                .setSoTimeout(500)       // 接收数据的等待超时时间，单位ms
//                .setSoLinger(60)         // 关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接，此时socket.close()是阻塞的
                .setSoKeepAlive(true)    // 开启监视TCP连接是否有效
                .build();
        connManager.setDefaultSocketConfig(socketConfig);

        final RequestConfig requestConfig = RequestConfig.custom()
                .setProxy(proxy)
                .setAuthenticationEnabled(authenticationEnabled)
                .setTargetPreferredAuthSchemes(targetPreferredAuthSchemes)
                .setProxyPreferredAuthSchemes(proxyPreferredAuthSchemes)
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setContentCompressionEnabled(contentCompressionEnabled)
                .build();

        // 禁用重试(参数：retryCount、requestSentRetryEnabled)
        final HttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(0, false);

        final CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(requestRetryHandler)
                .build();

        HTTPClient client = new HTTPClient(httpclient, requestConfig);
        if (logger.isDebugEnabled())
            logger.debug("HTTP client created: " + client);
        return client;
    }

    @Override
    public Class<HTTPClient> getObjectType() {
        return HTTPClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
