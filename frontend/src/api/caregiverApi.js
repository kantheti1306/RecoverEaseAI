import axiosInstance from './axiosInstance';

export const getCaregiverGuidance = (data) => axiosInstance.post('/api/caregiver/guidance', data);
