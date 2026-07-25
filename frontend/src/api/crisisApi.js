import axiosInstance from './axiosInstance';

export const sendCrisisInput = (data) => axiosInstance.post('/api/crisis/respond', data);
