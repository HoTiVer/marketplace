import { api } from "@/lib/api";
import {ForgotPasswordPayload, LoginPayload, RegisterPayload, ResetPasswordPayload} from '@/types/auth.types';

export const authService = {
    async login(data: LoginPayload) {
        const response = await api.post('/api/v1/auth/login', data);
        return response.data;
    },

    getOAuth2Url(provider: 'google' | 'github'):string {
        const baseUrl = process.env.NEXT_PUBLIC_API_URL;
        return `${baseUrl}/oauth2/authorization/${provider}`;
    },

    async register(data: RegisterPayload) {
        const response = await api.post('/api/v1/auth/register', data);
        return response.data;
    },

    async logout() {
        const response = await api.post('/api/v1/auth/logout');
        return response.data;
    },

    async getMe() {
        const response = await api.get('/api/v1/auth/me');
        return response.data;
    },

    async refresh() {
        const response = await api.post('/api/v1/auth/refresh');
        return response.data;
    },

    async requestPasswordReset(data: ForgotPasswordPayload) {
        const response = await api.post('/api/v1/auth/forgot-password', data);
        return response.data;
    },

    async resetPassword(data: ResetPasswordPayload) {
        const response = await api.post('/api/v1/auth/reset-password', data);
        return response.data;
    }
};