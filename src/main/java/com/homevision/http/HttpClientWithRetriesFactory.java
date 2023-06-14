package com.homevision.http;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import java.io.IOException;
import java.util.Set;

public class HttpClientWithRetriesFactory {
    // TODO: This could be configurable
    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(
            HttpStatus.SC_INTERNAL_SERVER_ERROR,
            HttpStatus.SC_BAD_GATEWAY,
            HttpStatus.SC_SERVICE_UNAVAILABLE,
            HttpStatus.SC_GATEWAY_TIMEOUT);

    private final int retryCount;

    public HttpClientWithRetriesFactory(int retryCount) {
        this.retryCount = retryCount;
    }

    public CloseableHttpClient provide() {
        return HttpClientBuilder.create()
                .addInterceptorLast((HttpResponseInterceptor) (response, context) -> {
                    if (RETRYABLE_STATUS_CODES.contains(response.getStatusLine().getStatusCode())) {
                        throw new IOException("Status code " + response.getStatusLine().getStatusCode());
                    }
                }) // TODO: Configure backoff time
                .setRetryHandler(new StandardHttpRequestRetryHandler(this.retryCount, true))
                .build();
    }
}
