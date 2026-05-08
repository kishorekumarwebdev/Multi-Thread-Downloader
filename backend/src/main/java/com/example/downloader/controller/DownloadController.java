package com.example.downloader.controller;

import com.example.downloader.model.LogEvent;
import com.example.downloader.model.SystemMetrics;
import com.example.downloader.model.DownloadTask;
import com.example.downloader.service.DownloadManager;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/download")
@CrossOrigin(origins = "*") // For development
public class DownloadController {

    private final DownloadManager downloadManager;

    public DownloadController(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @PostMapping("/start")
    public DownloadTask startDownload(@RequestBody Map<String, Object> request) throws IOException {
        String url = (String) request.get("url");
        int threadCount = (int) request.getOrDefault("threadCount", 4);
        return downloadManager.startDownload(url, threadCount);
    }

    @PostMapping("/pause/{id}")
    public void pauseDownload(@PathVariable String id) {
        downloadManager.pauseDownload(id);
    }

    @PostMapping("/resume/{id}")
    public void resumeDownload(@PathVariable String id) {
        downloadManager.resumeDownload(id);
    }

    @GetMapping("/status/{id}")
    public DownloadTask getStatus(@PathVariable String id) {
        return downloadManager.getTask(id);
    }

    @GetMapping("/list")
    public List<DownloadTask> listDownloads() {
        return downloadManager.getAllTasks();
    }

    @GetMapping("/metrics")
    public SystemMetrics getMetrics() {
        return downloadManager.getSystemMetrics();
    }

    @GetMapping("/system/logs")
    public List<LogEvent> getSystemLogs() {
        return downloadManager.getSystemLogs();
    }
}
