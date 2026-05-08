import React, { useState, useEffect } from 'react';
import { Play, Pause, Download, Activity, Clock, Database, Server } from 'lucide-react';
import { startDownload, pauseDownload, resumeDownload, listDownloads, formatBytes, formatTime } from './apiService';

const App = () => {
  const [url, setUrl] = useState('');
  const [threadCount, setThreadCount] = useState(4);
  const [downloads, setDownloads] = useState([]);

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const data = await listDownloads();
        setDownloads(data);
      } catch (e) {
        console.error('Failed to fetch downloads');
      }
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleStart = async () => {
    if (!url) return;
    try {
      await startDownload(url, threadCount);
      setUrl('');
    } catch (e) {
      alert('Error starting download');
    }
  };

  return (
    <div className="container">
      <div className="header">
        <h1>Antigravity Downloader</h1>
        <p>High-performance, multi-threaded file manager</p>
      </div>

      <div className="glass-card input-section">
        <input 
          type="text" 
          placeholder="Paste file URL here..." 
          value={url}
          onChange={(e) => setUrl(e.target.value)}
        />
        <select value={threadCount} onChange={(e) => setThreadCount(Number(e.target.value))}>
          {[1, 2, 4, 8, 10].map(n => (
            <option key={n} value={n}>{n} Threads</option>
          ))}
        </select>
        <button onClick={handleStart}>
          <Play size={18} style={{ marginRight: '8px', verticalAlign: 'middle' }} />
          Start
        </button>
      </div>

      <div className="download-list">
        {downloads.map(task => (
          <DownloadItem key={task.id} task={task} />
        ))}
      </div>
    </div>
  );
};

const DownloadItem = ({ task }) => {
  const progress = (task.downloadedSize / task.totalSize) * 100 || 0;

  return (
    <div className="glass-card download-item">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <div style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{task.fileName}</div>
        <div className={`status-badge status-${task.status.toLowerCase()}`}>
          {task.status}
        </div>
      </div>

      <div className="stats-grid">
        <div>
          <Activity size={14} style={{ marginRight: '4px' }} />
          Speed
          <span className="stat-val">{formatBytes(task.totalSpeed)}/s</span>
        </div>
        <div>
          <Database size={14} style={{ marginRight: '4px' }} />
          Progress
          <span className="stat-val">{formatBytes(task.downloadedSize)} / {formatBytes(task.totalSize)}</span>
        </div>
        <div>
          <Clock size={14} style={{ marginRight: '4px' }} />
          Time Remaining
          <span className="stat-val">{formatTime(task.remainingTime)}</span>
        </div>
      </div>

      <div className="progress-container">
        <div className="progress-bar" style={{ width: `${progress}%` }}></div>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        {task.status === 'DOWNLOADING' ? (
          <button onClick={() => pauseDownload(task.id)} style={{ background: 'rgba(255,255,255,0.1)', color: 'white' }}>
            <Pause size={16} /> Pause
          </button>
        ) : task.status === 'PAUSED' ? (
          <button onClick={() => resumeDownload(task.id)} style={{ background: 'var(--success)', color: 'white' }}>
            <Play size={16} /> Resume
          </button>
        ) : null}
      </div>

      <div className="thread-grid">
        {task.chunks.map(chunk => (
          <div key={chunk.threadId} className="thread-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
              <span style={{ opacity: 0.7 }}>Thread {chunk.threadId + 1}</span>
              <span style={{ color: chunk.completed ? 'var(--success)' : 'var(--primary)' }}>
                {chunk.completed ? 'Done' : formatBytes(chunk.speed) + '/s'}
              </span>
            </div>
            <div className="progress-container" style={{ height: '4px', margin: '4px 0' }}>
              <div 
                className="progress-bar" 
                style={{ 
                  width: `${(chunk.downloadedBytes / (chunk.endByte - chunk.startByte + 1)) * 100}%`,
                  background: chunk.completed ? 'var(--success)' : 'var(--secondary)'
                }}
              ></div>
            </div>
            <div style={{ fontSize: '0.65rem', opacity: 0.5 }}>
              Range: {formatBytes(chunk.startByte)} - {formatBytes(chunk.endByte)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default App;
