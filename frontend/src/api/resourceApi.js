import axiosInstance from './axiosInstance';

export const getResources = (params) => axiosInstance.get('/api/resources', { params });
export const explainTopic = (question) => axiosInstance.post('/api/resources/explain', { question });
