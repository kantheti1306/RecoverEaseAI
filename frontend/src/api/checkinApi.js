import axiosInstance from './axiosInstance';

export const submitCheckIn = (data) => axiosInstance.post('/api/checkins', data);
export const getCheckInHistory = () => axiosInstance.get('/api/checkins/history');
