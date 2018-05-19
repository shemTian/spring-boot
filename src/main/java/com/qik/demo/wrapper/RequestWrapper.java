package com.qik.demo.wrapper;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * RequestWrapper
 * 构建可重用的request</br>
 * 解决request.getInputStream 一次读取结束，流就为空
 *
 * @author tianshunqian
 * @version 1.0
 * 创建时间 2018/5/1 15:57
 **/
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;
    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = getBodyString(request);
    }

    public String getBodyString() {
        try {
            return new String(body, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("解析request-body出错",ex);
            return new String(body);
        }
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {

        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
    private byte[] getBodyString(ServletRequest request) {
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            //读入输出流中在通过输出流，获得inputStream的真实长度，否则自定义byte数组可能导致读取不完整，或者byte数组过大，导致jackson解析出问题
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buff[] = new byte[ 1024 ];
            int read;
            while( ( read = inputStream.read( buff ) ) > 0 ) {
                baos.write( buff, 0, read );
            }
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("解析request-body出错",e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭留出错",e);
                }
            }
        }
        return new byte[]{};
    }
}
