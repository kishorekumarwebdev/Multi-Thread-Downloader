package com.example.downloader.model;

public class LogEvent {
    private long timestamp;
    private String level;
    private String message;
    private String threadId;
    private String type; // e.g., "HTTP_REQUEST", "RETRY", "ERROR", "CHUNK_ALLOCATION"

    public LogEvent() {}

    public LogEvent(long timestamp, String level, String message, String threadId, String type) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.threadId = threadId;
        this.type = type;
    }

    // Getters and Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
