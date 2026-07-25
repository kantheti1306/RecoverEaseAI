import axiosInstance from './axiosInstance';

export const getProfile = () => axiosInstance.get('/api/user/profile');
export const completeOnboarding = (data) => axiosInstance.post('/api/user/onboarding', data);
