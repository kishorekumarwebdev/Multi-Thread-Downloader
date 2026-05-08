package com.example.downloader.model;

public class SystemMetrics {
    private int activeThreads;
    private int queueSize;
    private long totalDownloaded;
    private double totalSpeed;
    private long freeMemory;
    private long totalMemory;
    private long maxMemory;

    public SystemMetrics() {}

    public SystemMetrics(int activeThreads, int queueSize, long totalDownloaded, double totalSpeed, long freeMemory, long totalMemory, long maxMemory) {
        this.activeThreads = activeThreads;
        this.queueSize = queueSize;
        this.totalDownloaded = totalDownloaded;
        this.totalSpeed = totalSpeed;
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
        this.maxMemory = maxMemory;
    }

    public static SystemMetricsBuilder builder() {
        return new SystemMetricsBuilder();
    }

    public static class SystemMetricsBuilder {
        private int activeThreads;
        private int queueSize;
        private long totalDownloaded;
        private double totalSpeed;
        private long freeMemory;
        private long totalMemory;
        private long maxMemory;

        public SystemMetricsBuilder activeThreads(int activeThreads) { this.activeThreads = activeThreads; return this; }
        public SystemMetricsBuilder queueSize(int queueSize) { this.queueSize = queueSize; return this; }
        public SystemMetricsBuilder totalDownloaded(long totalDownloaded) { this.totalDownloaded = totalDownloaded; return this; }
        public SystemMetricsBuilder totalSpeed(double totalSpeed) { this.totalSpeed = totalSpeed; return this; }
        public SystemMetricsBuilder freeMemory(long freeMemory) { this.freeMemory = freeMemory; return this; }
        public SystemMetricsBuilder totalMemory(long totalMemory) { this.totalMemory = totalMemory; return this; }
        public SystemMetricsBuilder maxMemory(long maxMemory) { this.maxMemory = maxMemory; return this; }

        public SystemMetrics build() {
            return new SystemMetrics(activeThreads, queueSize, totalDownloaded, totalSpeed, freeMemory, totalMemory, maxMemory);
        }
    }

    // Getters
    public int getActiveThreads() { return activeThreads; }
    public int getQueueSize() { return queueSize; }
    public long getTotalDownloaded() { return totalDownloaded; }
    public double getTotalSpeed() { return totalSpeed; }
    public long getFreeMemory() { return freeMemory; }
    public long getTotalMemory() { return totalMemory; }
    public long getMaxMemory() { return maxMemory; }
}
