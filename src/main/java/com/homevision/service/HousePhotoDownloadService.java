package com.homevision.service;

import com.homevision.http.AsyncHttpFileDownloader;
import com.homevision.http.HousesApiClient;
import com.homevision.model.House;
import com.homevision.model.HouseCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

public class HousePhotoDownloadService {
    private static final Logger LOG = LogManager.getLogger();
    private final HousesApiClient housesApiClient;
    private final AsyncHttpFileDownloader fileDownloader;
    private final String downloadDestinationPath;

    public HousePhotoDownloadService(HousesApiClient housesApiClient, AsyncHttpFileDownloader fileDownloader, String downloadDestinationPath) {
        this.housesApiClient = housesApiClient;
        this.fileDownloader = fileDownloader;
        this.downloadDestinationPath = downloadDestinationPath; // TODO: This could be generalized to more than just a directory in FS
    }

    public void downloadPhotos(int firstPage, int lastPage, int pageSize) {
        createDirectoryIfNotExists(downloadDestinationPath);

        try {
            IntStream.range(firstPage, lastPage+1)
                    .parallel() // This may overload the API, can be removed to make this stream sequential
                    .forEach(pageNumber -> downloadPhotosForPage(housesApiClient, fileDownloader, pageNumber, pageSize));
        } finally {
            fileDownloader.awaitDownloadsToComplete(); // TODO: Could be done with AutoClosable
        }
    }

    private void createDirectoryIfNotExists(String destinationDir) {
        try {
            Files.createDirectories(Paths.get(destinationDir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create image download directory.", e);
        }
    }

    private void downloadPhotosForPage(HousesApiClient housesApiClient, AsyncHttpFileDownloader fileDownloader, int pageNumber, int pageSize) {
        LOG.info(String.format("Fetching page %s (size=%s) from houses API", pageNumber, pageSize));
        HouseCollection collection = housesApiClient.fetchHouses(pageNumber, pageSize);
        collection.houses().stream()
                .filter(h -> h.id() != null && h.address() != null && h.photoUrl() != null)
                .forEach(h -> downloadHousePhoto(fileDownloader, h));
    }

    private void downloadHousePhoto(AsyncHttpFileDownloader fileDownloader, House house) {
        try {
            String destinationPath = buildDestinationFilePath(house);
            URI sourceUri = new URI(house.photoUrl());
            fileDownloader.download(sourceUri, destinationPath);
        } catch (URISyntaxException e) {
            // Don't make the entire app fail because of 1 photo fails (TODO: Maybe need to catch some runtime exceptions here too)
            LOG.error(String.format("Failed to parse photo URL for house id %s: %s.", house.id(), house.photoUrl()), e);
        }
    }

    private String buildDestinationFilePath(House house) {
        String[] split = house.photoUrl().split("\\.");
        String extension = split[split.length - 1];
        return Path.of(downloadDestinationPath, String.format("%s-%s.%s", house.id(), house.address(), extension)).toString();
    }
}
