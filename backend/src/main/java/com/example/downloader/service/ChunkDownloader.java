package com.example.downloader.service;

import com.example.downloader.model.ChunkInfo;
import com.example.downloader.model.DownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ChunkDownloader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChunkDownloader.class);
    private final DownloadTask task;
    private final ChunkInfo chunk;
    private final String storagePath;
    private final DownloadManager downloadManager;

    public ChunkDownloader(DownloadTask task, ChunkInfo chunk, String storagePath, DownloadManager downloadManager) {
        this.task = task;
        this.chunk = chunk;
        this.storagePath = storagePath;
        this.downloadManager = downloadManager;
    }

    @Override
    public void run() {
        if (chunk.isCompleted()) return;

        try {
            URL url = URI.create(task.getUrl()).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            long currentStart = chunk.getStartByte() + chunk.getDownloadedBytes();
            if (currentStart > chunk.getEndByte()) {
                chunk.setCompleted(true);
                return;
            }

            connection.setRequestProperty("Range", "bytes=" + currentStart + "-" + chunk.getEndByte());
            downloadManager.addLog(task.getId(), "Thread " + chunk.getThreadId() + " sending HTTP Range: bytes=" + currentStart + "-" + chunk.getEndByte(), "HTTP_REQUEST", "DEBUG");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("Thread {}: Server returned invalid response code: {}", chunk.getThreadId(), responseCode);
                return;
            }

            try (InputStream inputStream = connection.getInputStream();
                 RandomAccessFile raf = new RandomAccessFile(storagePath + "/" + task.getFileName(), "rw")) {
                
                raf.seek(currentStart);
                byte[] buffer = new byte[8192];
                int bytesRead;
                long lastTime = System.currentTimeMillis();
                long bytesSinceLastCalc = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, bytesRead);
                    chunk.setDownloadedBytes(chunk.getDownloadedBytes() + bytesRead);
                    
                    // Periodic stats update
                    bytesSinceLastCalc += bytesRead;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTime >= 1000) {
                        chunk.setSpeed(bytesSinceLastCalc / ((currentTime - lastTime) / 1000.0));
                        bytesSinceLastCalc = 0;
                        lastTime = currentTime;
                    }

                    // Check for pause/stop signals (simplified for now)
                    if (Thread.currentThread().isInterrupted()) {
                        logger.info("Thread {} interrupted.", chunk.getThreadId());
                        return;
                    }
                }
                
                chunk.setCompleted(true);
                chunk.setSpeed(0);
                downloadManager.addLog(task.getId(), "Thread " + chunk.getThreadId() + " completed its range.", "CHUNK_ALLOCATION", "INFO");
                logger.info("Thread {} completed chunk.", chunk.getThreadId());
            }
        } catch (javax.net.ssl.SSLHandshakeException e) {
            String errorMsg = "Thread " + chunk.getThreadId() + " SSL Error: Certificate is invalid or expired.";
            downloadManager.addLog(task.getId(), errorMsg, "ERROR", "ERROR");
            logger.error(errorMsg);
        } catch (java.net.SocketTimeoutException e) {
            String errorMsg = "Thread " + chunk.getThreadId() + " timeout: Server took too long to respond.";
            downloadManager.addLog(task.getId(), errorMsg, "ERROR", "ERROR");
            logger.error(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Thread " + chunk.getThreadId() + " unexpected error: " + e.getMessage();
            downloadManager.addLog(task.getId(), errorMsg, "ERROR", "ERROR");
            logger.error(errorMsg, e);
        }
    }
}
