package com.geek.snails.upm.util;

import com.geek.snails.upm.exception.RemoteHttpCallException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static final SimpleResponseHandler responseHandler = new SimpleResponseHandler();

    public static String simpleProxyGet(String url, HttpClient httpClient) {
        return simpleProxyGet(url, responseHandler, httpClient);
    }

    public static <T> T simpleProxyGet(
            String url,
            ResponseHandler<T> handler,
            HttpClient httpClient
    ) {
        final URI uri = URI.create(url);
        HttpGet httpGet = new HttpGet(uri);

        T result = null;
        try {
            result = httpClient.execute(httpGet, handler);
        } catch (IOException e) {
            logger.error("Searching through:{} error,", url, e);
            throw new RemoteHttpCallException(e);
        } finally {
            httpGet.releaseConnection();
        }

        return result;
    }

    static class SimpleResponseHandler implements ResponseHandler<String> {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException,
                IOException {
            final StatusLine statusLine = response.getStatusLine();
            final HttpEntity entity = response.getEntity();
            if (statusLine.getStatusCode() >= 300) {
                EntityUtils.consume(entity);
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase()
                );
            }
            return entity == null ? null : EntityUtils.toString(entity, "UTF-8");
        }
    }
}
