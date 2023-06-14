package com.homevision.service;

import com.homevision.http.AsyncHttpFileDownloader;
import com.homevision.http.HousesApiClient;
import com.homevision.model.House;
import com.homevision.model.HouseCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class HousePhotoDownloadServiceTest {
    private static String DOWNLOAD_PATH;

    private HousesApiClient housesApiClient;
    private AsyncHttpFileDownloader fileDownloader;
    private HousePhotoDownloadService service;

    @BeforeAll
    static void setUpAll() throws IOException {
        DOWNLOAD_PATH = Files.createTempDirectory(null).toString();
    }

    @BeforeEach
    void setUpEach() {
        housesApiClient = mock(HousesApiClient.class);
        fileDownloader = mock(AsyncHttpFileDownloader.class);
        service = new HousePhotoDownloadService(housesApiClient, fileDownloader, DOWNLOAD_PATH);
    }

    @Test
    public void givenHousesApiCallOkWhenDownloadOnePageThenPhotosDownloaded() {
        String photoUrl = "http://photoUrl.jpg";
        when(housesApiClient.fetchHouses(1, 10))
                .thenReturn(new HouseCollection(List.of(
                        new House(1L, "address", "owner", 1234L, photoUrl)), true));

        service.downloadPhotos(1, 1, 10);

        verify(fileDownloader).download(URI.create(photoUrl),
                Path.of(DOWNLOAD_PATH, "1-address.jpg").toString());
        verify(fileDownloader).awaitDownloadsToComplete();
    }

    @Test
    public void givenHousesApiCallOkWhenDownloadManyPagesThenPhotosDownloaded() {
        String photoUrl1 = "http://photoUrl1.jpg";
        String photoUrl2 = "http://photoUrl2.jpg";
        when(housesApiClient.fetchHouses(1, 10))
                .thenReturn(new HouseCollection(List.of(
                        new House(1L, "address", "owner", 1234L, photoUrl1)), true));
        when(housesApiClient.fetchHouses(2, 10))
                .thenReturn(new HouseCollection(List.of(
                        new House(2L, "address", "owner", 1234L, photoUrl2)), true));

        service.downloadPhotos(1, 2, 10);

        verify(fileDownloader).download(URI.create(photoUrl1),
                Path.of(DOWNLOAD_PATH, "1-address.jpg").toString());
        verify(fileDownloader).download(URI.create(photoUrl2),
                Path.of(DOWNLOAD_PATH, "2-address.jpg").toString());
        verify(fileDownloader).awaitDownloadsToComplete();
    }

    @Test
    public void givenHousesApiCallFailWhenDownloadManyPagesThenDownloadPhotosUntilFailure() {
        String photoUrl = "http://photoUrl.jpg";
        when(housesApiClient.fetchHouses(1, 10))
                .thenReturn(new HouseCollection(List.of(
                        new House(1L, "address", "owner", 1234L, photoUrl)), true));
        RuntimeException expectedException = new RuntimeException("error");
        when(housesApiClient.fetchHouses(2, 10)).thenThrow(expectedException);

        var ex = assertThrows(RuntimeException.class, () -> service.downloadPhotos(1, 2, 10));

        assertEquals(expectedException, ex);
        verify(fileDownloader).download(URI.create(photoUrl),
                Path.of(DOWNLOAD_PATH, "1-address.jpg").toString());
        verify(fileDownloader).awaitDownloadsToComplete();
    }
}
