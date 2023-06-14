package com.homevision.http;

import com.homevision.model.HouseCollection;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HousesApiClientTest {
    private final HttpClientWithRetriesTestHelper helper = new HttpClientWithRetriesTestHelper();
    private final String testEndpointUrl = "http://something/houses";

    @Test
    void givenHttpStatusOkWhenFetchHousesThenReturnHousesCollection() throws IOException {
        URI uri = URI.create(testEndpointUrl + "?page=1&per_page=10");
        CloseableHttpClient httpClient = helper.mockHttpClientSuccess("GET", uri, "com/homevision/http/housesApiResponse200.json");
        HttpClientWithRetriesFactory httpClientFactory = helper.mockHttpClientFactory(httpClient);

        HousesApiClient housesApiClient = new HousesApiClient(httpClientFactory, testEndpointUrl);
        HouseCollection collection = housesApiClient.fetchHouses(1, 10);

        assertTrue(collection.ok());
        assertEquals(10, collection.houses().size());
        assertEquals(0L, collection.houses().get(0).id());
        assertEquals("4 Pumpkin Hill Street Antioch, TN 37013", collection.houses().get(0).address());
        assertEquals("Nicole Bone", collection.houses().get(0).homeOwner());
        assertEquals(105124L, collection.houses().get(0).price());
        assertEquals("https://image.shutterstock.com/image-photo/big-custom-made-luxury-house-260nw-374099713.jpg", collection.houses().get(0).photoUrl());
    }

    @Test
    void givenHttpStatus503WhenFetchHousesThenFail() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenThrow(new IOException());
        HttpClientWithRetriesFactory httpClientFactory = helper.mockHttpClientFactory(httpClient);

        HousesApiClient housesApiClient = new HousesApiClient(httpClientFactory, testEndpointUrl);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            housesApiClient.fetchHouses(1, 10);
        });
        assertEquals("Failed to execute request.", ex.getMessage());
    }
}
