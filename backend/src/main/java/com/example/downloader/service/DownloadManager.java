package com.example.downloader.service;

import com.example.downloader.model.ChunkInfo;
import com.example.downloader.model.DownloadStatus;
import com.example.downloader.model.DownloadTask;
import com.example.downloader.model.LogEvent;
import com.example.downloader.model.SystemMetrics;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.SSLHandshakeException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class DownloadManager {
    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    
    private final Map<String, DownloadTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> threadPools = new ConcurrentHashMap<>();
    private final List<LogEvent> systemLogs = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final StatePersistenceManager persistenceManager;

    @Value("${downloader.storage-path}")
    private String storagePath;

    public void addLog(String taskId, String message, String type, String level) {
        LogEvent event = new LogEvent(System.currentTimeMillis(), level, message, Thread.currentThread().getName(), type);
        if (taskId != null) {
            DownloadTask task = tasks.get(taskId);
            if (task != null) {
                task.getLogs().add(event);
                if (task.getLogs().size() > 100) task.getLogs().remove(0);
            }
        }
        systemLogs.add(event);
        if (systemLogs.size() > 200) systemLogs.remove(0);
        logger.info("[{}] {}", type, message);
    }

    public DownloadManager(StatePersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @PostConstruct
    public void init() {
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        tasks.putAll(persistenceManager.loadState());
    }

    public DownloadTask startDownload(String urlStr, int threadCount) throws IOException {
        long fileSize;
        String fileName;

        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                String errorMsg = "Server returned error code: " + responseCode;
                addLog(null, errorMsg, "ERROR", "ERROR");
                throw new IOException(errorMsg);
            }

            fileSize = connection.getContentLengthLong();
            fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || fileName.contains("?")) {
                fileName = "download_" + System.currentTimeMillis();
            }
        } catch (SSLHandshakeException e) {
            String errorMsg = "SSL Certificate Error: The server's certificate is invalid or expired. URL: " + urlStr;
            addLog(null, errorMsg, "ERROR", "ERROR");
            throw new IOException(errorMsg);
        } catch (IOException e) {
            String errorMsg = "Connection Error: Failed to connect to the server. " + e.getMessage();
            addLog(null, errorMsg, "ERROR", "ERROR");
            throw e;
        }

        if (fileSize <= 0) {
            String errorMsg = "Invalid file size received from server: " + fileSize;
            addLog(null, errorMsg, "ERROR", "ERROR");
            throw new IOException(errorMsg);
        }

        DownloadTask task = new DownloadTask();
        task.setUrl(urlStr);
        task.setFileName(fileName);
        task.setTotalSize(fileSize);
        task.setThreadCount(threadCount);
        task.setStatus(DownloadStatus.DOWNLOADING);

        List<ChunkInfo> chunks = new ArrayList<>();
        long chunkSize = fileSize / threadCount;
        for (int i = 0; i < threadCount; i++) {
            long start = i * chunkSize;
            long end = (i == threadCount - 1) ? fileSize - 1 : (start + chunkSize - 1);
            chunks.add(new ChunkInfo(i, start, end, 0, false, 0, 0));
        }
        task.setChunks(chunks);

        // Pre-allocate file
        try (RandomAccessFile raf = new RandomAccessFile(new File(storagePath, fileName), "rw")) {
            raf.setLength(fileSize);
        }

        tasks.put(task.getId(), task);
        executeTask(task);
        return task;
    }

    private void executeTask(DownloadTask task) {
        ExecutorService executor = Executors.newFixedThreadPool(task.getThreadCount());
        threadPools.put(task.getId(), executor);
        for (ChunkInfo chunk : task.getChunks()) {
            if (!chunk.isCompleted()) {
                executor.submit(new ChunkDownloader(task, chunk, storagePath, this));
            }
        }
    }

    public void pauseDownload(String taskId) {
        DownloadTask task = tasks.get(taskId);
        if (task != null && task.getStatus() == DownloadStatus.DOWNLOADING) {
            task.setStatus(DownloadStatus.PAUSED);
            ExecutorService executor = threadPools.remove(taskId);
            if (executor != null) {
                executor.shutdownNow();
            }
            persistenceManager.saveState(tasks);
        }
    }

    public void resumeDownload(String taskId) {
        DownloadTask task = tasks.get(taskId);
        if (task != null && task.getStatus() == DownloadStatus.PAUSED) {
            task.setStatus(DownloadStatus.DOWNLOADING);
            executeTask(task);
        }
    }

    public List<DownloadTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public DownloadTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    @Scheduled(fixedRate = 1000)
    public void updateProgress() {
        for (DownloadTask task : tasks.values()) {
            if (task.getStatus() == DownloadStatus.DOWNLOADING) {
                long downloaded = 0;
                double totalSpeed = 0;
                boolean allCompleted = true;
                
                for (ChunkInfo chunk : task.getChunks()) {
                    downloaded += chunk.getDownloadedBytes();
                    totalSpeed += chunk.getSpeed();
                    if (!chunk.isCompleted()) {
                        allCompleted = false;
                    }
                }
                
                task.setDownloadedSize(downloaded);
                task.setTotalSpeed(totalSpeed);
                
                if (totalSpeed > 0) {
                    task.setRemainingTime((long) ((task.getTotalSize() - downloaded) / totalSpeed));
                }
                
                // Track history for comparison
                task.getSpeedHistory().add(totalSpeed);
                if (task.getSpeedHistory().size() > 60) task.getSpeedHistory().remove(0);
                
                if (allCompleted) {
                    task.setStatus(DownloadStatus.COMPLETED);
                    task.setTotalSpeed(0);
                    ExecutorService executor = threadPools.remove(task.getId());
                    if (executor != null) executor.shutdown();
                }
            }
        }
        persistenceManager.saveState(tasks);
    }

    public SystemMetrics getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        int active = 0;
        for (ExecutorService es : threadPools.values()) {
            if (es instanceof ThreadPoolExecutor) {
                active += ((ThreadPoolExecutor) es).getActiveCount();
            }
        }

        long totalDownloaded = tasks.values().stream().mapToLong(DownloadTask::getDownloadedSize).sum();
        double totalSpeed = tasks.values().stream().mapToDouble(DownloadTask::getTotalSpeed).sum();

        return SystemMetrics.builder()
                .activeThreads(active)
                .queueSize(tasks.size())
                .totalDownloaded(totalDownloaded)
                .totalSpeed(totalSpeed)
                .freeMemory(runtime.freeMemory())
                .totalMemory(runtime.totalMemory())
                .maxMemory(runtime.maxMemory())
                .build();
    }

    public List<LogEvent> getSystemLogs() {
        return systemLogs;
    }
}
