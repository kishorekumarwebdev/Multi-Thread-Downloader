import React, { useState, useEffect, useRef } from 'react';
import { Play, Pause, Activity, Clock, Database, Terminal, Shield, RefreshCw, Layers } from 'lucide-react';
import { startDownload, pauseDownload, resumeDownload, listDownloads, getSystemMetrics, getSystemLogs, formatBytes, formatTime } from './apiService';

const App = () => {
  const [url, setUrl] = useState('');
  const [threadCount, setThreadCount] = useState(4);
  const [downloads, setDownloads] = useState([]);
  const [metrics, setMetrics] = useState(null);
  const [logs, setLogs] = useState([]);
  const [showDevMode, setShowDevMode] = useState(false);

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const [dlData, metricsData, logsData] = await Promise.all([
          listDownloads(),
          getSystemMetrics(),
          getSystemLogs()
        ]);
        setDownloads(dlData);
        setMetrics(metricsData);
        setLogs(logsData);
      } catch (e) {
        // Silent error for polling
      }
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleStart = async () => {
    if (!url) return;
    try {
      const response = await startDownload(url, threadCount);
      console.log('Download started:', response);
      setUrl('');
    } catch (e) {
      console.error('Error starting download:', e);
      alert('Error starting download: ' + (e.response?.data?.message || e.message));
    }
  };

  return (
    <div className="container">
      <div className="header">
        <h1>MU</h1>
        <p>High-performance, multi-threaded file manager</p>

        <div className="dev-toggle">
          <span>Dev Mode</span>
          <label className="switch">
            <input type="checkbox" checked={showDevMode} onChange={() => setShowDevMode(!showDevMode)} />
            <span className="slider"></span>
          </label>
        </div>
      </div>

      {showDevMode && metrics && (
        <div className="glass-card" style={{ marginBottom: '2rem' }}>
          <h3><Layers size={18} style={{ verticalAlign: 'middle', marginRight: '8px' }} /> System Dashboard</h3>
          <div className="metrics-grid">
            <div className="metric-card">
              <span className="metric-val">{metrics.activeThreads}</span>
              <span className="metric-label">Active Threads</span>
            </div>
            <div className="metric-card">
              <span className="metric-val">{metrics.queueSize}</span>
              <span className="metric-label">Download Queue</span>
            </div>
            <div className="metric-card">
              <span className="metric-val">{formatBytes(metrics.totalSpeed)}/s</span>
              <span className="metric-label">Total Speed</span>
            </div>
            <div className="metric-card">
              <span className="metric-val">{((metrics.totalMemory - metrics.freeMemory) / 1024 / 1024).toFixed(1)} MB</span>
              <span className="metric-label">Used Memory</span>
            </div>
          </div>
        </div>
      )}

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
          <DownloadItem key={task.id} task={task} showDevMode={showDevMode} />
        ))}
      </div>

      {showDevMode && logs.length > 0 && (
        <div className="log-panel">
          <div style={{ marginBottom: '0.5rem', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Terminal size={14} /> Internal System Logs
          </div>
          {logs.slice(-50).reverse().map(log => (
            <div key={log.timestamp} className="log-entry">
              <span className="log-timestamp">{new Date(log.timestamp).toLocaleTimeString()}</span>
              <span className={`log-type status-${log.level.toLowerCase()}`}>[{log.type}]</span>
              <span className="log-msg">{log.message}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const DownloadItem = ({ task, showDevMode }) => {
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
        <div><Activity size={14} style={{ marginRight: '4px' }} /> Speed <span className="stat-val">{formatBytes(task.totalSpeed)}/s</span></div>
        <div><Database size={14} style={{ marginRight: '4px' }} /> Progress <span className="stat-val">{formatBytes(task.downloadedSize)} / {formatBytes(task.totalSize)}</span></div>
        <div><Clock size={14} style={{ marginRight: '4px' }} /> Remaining <span className="stat-val">{formatTime(task.remainingTime)}</span></div>
      </div>

      <div className="progress-container">
        <div className="progress-bar" style={{ width: `${progress}%` }}></div>
      </div>

      {showDevMode && task.speedHistory && task.speedHistory.length > 1 && (
        <div style={{ marginTop: '1rem' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>Speed Comparison History</span>
          <div className="graph-container">
            <svg width="100%" height="100%" viewBox="0 0 100 100" preserveAspectRatio="none">
              <polyline
                points={task.speedHistory.map((s, i) => `${(i / (task.speedHistory.length - 1)) * 100},${100 - (s / Math.max(...task.speedHistory, 1)) * 100}`).join(' ')}
                className="graph-line"
              />
            </svg>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', gap: '0.5rem', margin: '1rem 0' }}>
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
              {showDevMode && chunk.retryCount > 0 && (
                <span style={{ color: 'var(--error)', fontSize: '0.7rem' }}>Retries: {chunk.retryCount}</span>
              )}
              <span style={{ color: chunk.completed ? 'var(--success)' : 'var(--primary)' }}>
                {chunk.completed ? 'Done' : formatBytes(chunk.speed) + '/s'}
              </span>
            </div>
            <div className="progress-container" style={{ height: '4px', margin: '4px 0' }}>
              <div
                className="progress-bar"
                style={{
                  width: `${(chunk.downloadedBytes / (chunk.endByte - chunk.startByte + 1)) * 100}%`,
                  background: chunk.completed ? 'var(--success)' : (chunk.downloadedBytes > 0 ? 'var(--secondary)' : 'rgba(255,255,255,0.1)')
                }}
              ></div>
            </div>
            <div style={{ fontSize: '0.65rem', opacity: 0.5, display: 'flex', justifyContent: 'space-between' }}>
              <span>{formatBytes(chunk.startByte)} - {formatBytes(chunk.endByte)}</span>
              {showDevMode && <span>{((chunk.downloadedBytes / (chunk.endByte - chunk.startByte + 1)) * 100).toFixed(1)}%</span>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default App;
