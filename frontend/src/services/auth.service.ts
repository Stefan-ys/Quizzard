import axiosInstance from './axiosInstance';
import { API_LOGIN_URL, API_LOGOUT_URL, API_REFRESH_TOKEN_URL, API_REGISTER_URL } from '../common/urls';
import { LoginRequest, RegisterRequest } from '../types/auth.type';
import axios from 'axios';

export const register = async (registerData: RegisterRequest) => {
    try {
        const response = await axiosInstance.post(API_REGISTER_URL, registerData);
        return response.data;
    } catch (error: any) {
        throw new Error(error.response?.data?.message || 'Registration failed');
    }
};

export const login = async (loginData: LoginRequest) => {
    try {
        const response = await axiosInstance.post(API_LOGIN_URL, loginData);
        return response.data;
    } catch (error: any) {
        throw new Error(error.response?.data?.message || 'Login failed');
    }
};

export const logout = async () => {
    try {
        await axiosInstance.post(API_LOGOUT_URL);
    } catch (error: any) {
        throw new Error('Logout failed');
    }
};

export const refreshToken = async (): Promise<{ accessToken: string; refreshToken: string } | null> => {
    try {
        const refreshToken = localStorage.getItem('refreshToken');

        const response = await axios.post(
            `${API_REFRESH_TOKEN_URL}`,
            { requestRefreshToken: refreshToken },
            { 
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${refreshToken}`
                } 
            }
        );
        return response.data;
    } catch (error) {
        console.error('Token refresh failed', error);
        return null;
    }
};
