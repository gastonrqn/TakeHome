package com.homevision.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

public class HttpFileDownloader {
    private static final Logger LOG = LogManager.getLogger();
    private final HttpClientWithRetriesFactory httpClientWithRetriesFactory;

    public HttpFileDownloader(HttpClientWithRetriesFactory httpClientWithRetriesFactory) {
        this.httpClientWithRetriesFactory = httpClientWithRetriesFactory;
    }

    public void download(URI sourceUri, String destination) {
        // TODO: Security concern: only download from trusted sources and check mime type
        executeRequest(new HttpGet(sourceUri), response -> writeToDestination(destination, response));
    }

    private void writeToDestination(String destination, CloseableHttpResponse response) {
        try {
            // Using channels to avoid loading the whole file in memory
            ReadableByteChannel readableByteChannel = Channels.newChannel(response.getEntity().getContent());
            try (FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
            LOG.info(String.format("Successfully downloaded to %s", destination));
        } catch (IOException e) {
            throw new IllegalStateException("File download failed. ", e);
        }
    }

    private void executeRequest(HttpUriRequest request, Consumer<CloseableHttpResponse> responseConsumer) {
        try (CloseableHttpClient client = httpClientWithRetriesFactory.provide()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                responseConsumer.accept(response);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute request.", e);
        }
    }
}
