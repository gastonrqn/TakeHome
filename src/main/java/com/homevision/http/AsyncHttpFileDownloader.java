package com.homevision.http;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncHttpFileDownloader {
    private final ExecutorService executorService;
    private final CompletionService<String> completionService;
    private final HttpFileDownloader fileDownloader;
    private final AtomicInteger downloadsCount;

    public AsyncHttpFileDownloader(int threadPoolSize, HttpFileDownloader fileDownloader) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.completionService = new ExecutorCompletionService<>(executorService);
        this.fileDownloader = fileDownloader;
        this.downloadsCount = new AtomicInteger(0);
    }

    public void download(URI sourceUri, String destination) {
        completionService.submit(() -> fileDownloader.download(sourceUri, destination), null);
        downloadsCount.incrementAndGet();
    }

    public void awaitDownloadsToComplete() {
        try {
            while(downloadsCount.getAndDecrement() != 0) {
                completionService.take().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("An error occurred while awaiting downloads to complete.", e);
        } finally {
            executorService.shutdownNow();
        }
    }
}
