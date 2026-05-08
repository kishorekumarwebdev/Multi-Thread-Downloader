package com.example.downloader.model;

public class ChunkInfo {
    private int threadId;
    private long startByte;
    private long endByte;
    private long downloadedBytes;
    private boolean completed;
    private double speed; // Bytes per second for this thread
    private int retryCount;

    public ChunkInfo() {}

    public ChunkInfo(int threadId, long startByte, long endByte, long downloadedBytes, boolean completed, double speed, int retryCount) {
        this.threadId = threadId;
        this.startByte = startByte;
        this.endByte = endByte;
        this.downloadedBytes = downloadedBytes;
        this.completed = completed;
        this.speed = speed;
        this.retryCount = retryCount;
    }

    // Getters and Setters
    public int getThreadId() { return threadId; }
    public void setThreadId(int threadId) { this.threadId = threadId; }
    public long getStartByte() { return startByte; }
    public void setStartByte(long startByte) { this.startByte = startByte; }
    public long getEndByte() { return endByte; }
    public void setEndByte(long endByte) { this.endByte = endByte; }
    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
