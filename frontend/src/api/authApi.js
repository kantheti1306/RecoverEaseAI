import axiosInstance from './axiosInstance';

export const login = (data) => axiosInstance.post('/api/auth/login', data);
export const signup = (data) => axiosInstance.post('/api/auth/signup', data);
