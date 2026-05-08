# Java Multi-thread File Downloader

## Project Overview

Java Multi-thread File Downloader is a backend-focused Java project that downloads a file by splitting it into multiple chunks and downloading those chunks using separate threads.

The main goal of this project is to demonstrate the use of Java multithreading, file handling, chunk-based downloading, and basic performance improvement compared to single-threaded downloading.

## Features

- Download files using multiple threads
- Split a file into multiple byte ranges
- Assign each chunk to a separate thread
- Track individual thread progress
- Merge downloaded chunks into a final file
- Handle retry logic for failed chunks
- Display download status and speed
- Maven-based Java project structure

## Tech Stack

- Java
- Multithreading
- Maven
- File Handling
- HTTP Connection

## Why I Built This Project

I built this project to understand how download managers work internally. Instead of downloading a file in one continuous stream, this project splits the file into smaller parts and downloads them in parallel.

This helped me learn practical concepts like thread creation, byte-range requests, file writing, chunk management, and synchronization.

## How It Works

1. The user provides a file URL.
2. The program checks the total file size.
3. The file is divided into multiple chunks.
4. Each chunk is assigned to a separate thread.
5. Every thread downloads its assigned byte range.
6. The downloaded chunks are stored temporarily.
7. After all chunks are completed, they are merged into one final file.

## Project Structure

```text
d:
└── Multi_thread_download/
    ├── README.md
    ├── backend/
    │   ├── download_state.json
    │   ├── pom.xml
    │   ├── TestUrl.java
    │   ├── downloads/
    │   │   └── download
    │   └── src/
    │       └── main/
    │           ├── java/
    │           │   └── com/
    │           │       └── example/
    │           │           └── downloader/
    │           │               ├── AppConfig.java
    │           │               ├── DownloaderApplication.java
    │           │               ├── controller/
    │           │               │   └── DownloadController.java
    │           │               ├── model/
    │           │               │   ├── ChunkInfo.java
    │           │               │   ├── DownloadStatus.java
    │           │               │   ├── DownloadTask.java
    │           │               │   ├── LogEvent.java
    │           │               │   └── SystemMetrics.java
    │           │               └── service/
    │           │                   ├── ChunkDownloader.java
    │           │                   ├── DownloadManager.java
    │           │                   └── StatePersistenceManager.java
    │           └── resources/
    │               └── application.properties
    ├── frontend/
    │   ├── index.html
    │   ├── package.json
    │   ├── vite.config.js
    │   └── src/
    │       ├── apiService.js
    │       ├── App.css
    │       ├── App.jsx
    │       ├── index.css
    │       └── main.jsx
    └── frontend_v3/
        ├── index.html
        ├── package.json
        ├── vite.config.js
        └── src/
            ├── apiService.js
            ├── App.jsx
            ├── index.css
            └── main.jsx
```