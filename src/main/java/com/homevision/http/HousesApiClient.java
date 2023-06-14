package com.homevision.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homevision.model.HouseCollection;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

public class HousesApiClient {
    private static final Logger LOG = LogManager.getLogger();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final HttpClientWithRetriesFactory httpClientWithRetriesFactory;
    private final URI endpointUri;

    public HousesApiClient(HttpClientWithRetriesFactory httpClientWithRetriesFactory, String endpointUrl) {
        this.httpClientWithRetriesFactory = httpClientWithRetriesFactory;
        this.endpointUri = buildUri(endpointUrl);
    }

    private URI buildUri(String endpointUrl) {
        try {
            return new URIBuilder(endpointUrl).build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public HouseCollection fetchHouses(Integer pageNumber, Integer pageSize) {
        URI url = buildGetHousesUrl(pageNumber, pageSize);
        return executeRequest(new HttpGet(url), HousesApiClient::extractHouseCollection);
    }

    private URI buildGetHousesUrl(Integer pageNumber, Integer pageSize) {
        try {
            return new URIBuilder(endpointUri)
                    .addParameter("page", pageNumber.toString())
                    .addParameter("per_page", pageSize.toString())
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T executeRequest(HttpUriRequest request, Function<CloseableHttpResponse, T> responseConsumer) {
        try (CloseableHttpClient client = httpClientWithRetriesFactory.provide()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                return responseConsumer.apply(response);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute request.", e);
        }
    }

    private static HouseCollection extractHouseCollection(CloseableHttpResponse response) {
        try {
            return OBJECT_MAPPER.readValue(response.getEntity().getContent(), HouseCollection.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected response body from houses API.", e);
        }
    }
}
