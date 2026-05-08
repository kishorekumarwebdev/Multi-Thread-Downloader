package com.example.downloader.model;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloadTask {
    private String id = UUID.randomUUID().toString();
    private String url;
    private String fileName;
    private long totalSize;
    private long downloadedSize;
    private int threadCount;
    private DownloadStatus status;
    private List<ChunkInfo> chunks;
    private double totalSpeed;
    private long remainingTime; // in seconds
    private List<LogEvent> logs = new CopyOnWriteArrayList<>();
    private List<Double> speedHistory = new ArrayList<>();

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
    public long getDownloadedSize() { return downloadedSize; }
    public void setDownloadedSize(long downloadedSize) { this.downloadedSize = downloadedSize; }
    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    public DownloadStatus getStatus() { return status; }
    public void setStatus(DownloadStatus status) { this.status = status; }
    public List<ChunkInfo> getChunks() { return chunks; }
    public void setChunks(List<ChunkInfo> chunks) { this.chunks = chunks; }
    public double getTotalSpeed() { return totalSpeed; }
    public void setTotalSpeed(double totalSpeed) { this.totalSpeed = totalSpeed; }
    public long getRemainingTime() { return remainingTime; }
    public void setRemainingTime(long remainingTime) { this.remainingTime = remainingTime; }
    public List<LogEvent> getLogs() { return logs; }
    public void setLogs(List<LogEvent> logs) { this.logs = logs; }
    public List<Double> getSpeedHistory() { return speedHistory; }
    public void setSpeedHistory(List<Double> speedHistory) { this.speedHistory = speedHistory; }
}
