import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/download';

export const startDownload = async (url, threadCount) => {
    const response = await axios.post(`${API_BASE_URL}/start`, { url, threadCount });
    return response.data;
};

export const pauseDownload = async (id) => {
    await axios.post(`${API_BASE_URL}/pause/${id}`);
};

export const resumeDownload = async (id) => {
    await axios.post(`${API_BASE_URL}/resume/${id}`);
};

export const listDownloads = async () => {
    const response = await axios.get(`${API_BASE_URL}/list`);
    return response.data;
};

export const getSystemMetrics = async () => {
    const response = await axios.get(`${API_BASE_URL}/metrics`);
    return response.data;
};

export const getSystemLogs = async () => {
    const response = await axios.get(`${API_BASE_URL}/system/logs`);
    return response.data;
};

export const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

export const formatTime = (seconds) => {
    if (!seconds || seconds === Infinity) return '--:--';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    return [h, m, s].map(v => v < 10 ? '0' + v : v).filter((v, i) => v !== '00' || i > 0).join(':');
};
