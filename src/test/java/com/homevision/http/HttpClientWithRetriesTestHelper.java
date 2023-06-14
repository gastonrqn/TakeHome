package com.homevision.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpClientWithRetriesTestHelper {
    public HttpClientWithRetriesFactory mockHttpClientFactory(CloseableHttpClient httpClient) {
        HttpClientWithRetriesFactory httpClientFactory = mock(HttpClientWithRetriesFactory.class);
        when(httpClientFactory.provide()).thenReturn(httpClient);
        return httpClientFactory;
    }

    public CloseableHttpClient mockHttpClientSuccess(String method, URI uri, String responsePayloadPath) throws IOException {
        InputStream responsePayload = loadResource(responsePayloadPath);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(responsePayload);
        CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
        when(responseMock.getEntity()).thenReturn(entity);
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        when(closeableHttpClient.execute(argThat(req -> req.getMethod().equals(method) && req.getURI().equals(uri))))
                .thenReturn(responseMock);
        return closeableHttpClient;
    }

    private InputStream loadResource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }
}
