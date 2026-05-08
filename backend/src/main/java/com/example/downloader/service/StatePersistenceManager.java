package com.example.downloader.service;

import com.example.downloader.model.DownloadTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatePersistenceManager {
    private static final Logger logger = LoggerFactory.getLogger(StatePersistenceManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${downloader.state-file}")
    private String stateFile;

    public void saveState(Map<String, DownloadTask> tasks) {
        try {
            objectMapper.writeValue(new File(stateFile), tasks.values());
        } catch (IOException e) {
            logger.error("Failed to save download state: {}", e.getMessage());
        }
    }

    public Map<String, DownloadTask> loadState() {
        File file = new File(stateFile);
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }

        try {
            List<DownloadTask> tasks = objectMapper.readValue(file, new TypeReference<List<DownloadTask>>() {});
            Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();
            for (DownloadTask task : tasks) {
                taskMap.put(task.getId(), task);
            }
            return taskMap;
        } catch (IOException e) {
            logger.error("Failed to load download state: {}", e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
}
