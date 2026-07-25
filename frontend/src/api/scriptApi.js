import axiosInstance from './axiosInstance';

export const generateScript = (data) => axiosInstance.post('/api/scripts/generate', data);
export const getScriptHistory = () => axiosInstance.get('/api/scripts/history');
