package com.homevision;

import com.homevision.http.AsyncHttpFileDownloader;
import com.homevision.http.HousesApiClient;
import com.homevision.http.HttpClientWithRetriesFactory;
import com.homevision.http.HttpFileDownloader;
import com.homevision.service.HousePhotoDownloadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Application {
    private static final Logger LOG = LogManager.getLogger();

    // TODO: Take from config file
    private static final int DEFAULT_RETRY_COUNT = 15;
    private static final int DEFAULT_PAGE_COUNT = 10;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int IMAGE_DOWNLOAD_MAX_THREADS = 15;
    private static final String IMAGE_DOWNLOAD_DIRECTORY = "imagedownload";
    private static final String HOUSES_ENDPOINT_URL = "http://app-homevision-staging.herokuapp.com/api_project/houses";

    public static void main(String[] args) {
        new Application().run();
    }

    public void run() {
        LOG.info("Starting application");
        HttpClientWithRetriesFactory httpClientWithRetriesFactory = new HttpClientWithRetriesFactory(DEFAULT_RETRY_COUNT);
        HousesApiClient housesApiClient = new HousesApiClient(httpClientWithRetriesFactory, HOUSES_ENDPOINT_URL);
        AsyncHttpFileDownloader fileDownloader = new AsyncHttpFileDownloader(IMAGE_DOWNLOAD_MAX_THREADS, new HttpFileDownloader(httpClientWithRetriesFactory));
        HousePhotoDownloadService service = new HousePhotoDownloadService(housesApiClient, fileDownloader, IMAGE_DOWNLOAD_DIRECTORY);
        LOG.info("Starting photo download");
        service.downloadPhotos(1, DEFAULT_PAGE_COUNT, DEFAULT_PAGE_SIZE);
        LOG.info("Finished successfully");
    }


}