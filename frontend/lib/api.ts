import axios from "axios";
import { toast } from "sonner";

const getApiUrl = () => {
    if (typeof window === "undefined") {
        return process.env.API_INTERNAL_URL;
    }

    return process.env.NEXT_PUBLIC_API_URL;
};

export const api = axios.create({
    baseURL: getApiUrl(),
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    if (config.data instanceof FormData) {
        delete config.headers["Content-Type"];
    } else if (!config.headers["Content-Type"]) {
        config.headers["Content-Type"] = "application/json";
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const status = error.response?.status;

        const isLoginRequest = originalRequest.url.includes('/api/v1/auth/login');
        const isRefreshRequest = originalRequest.url.includes('/api/v1/auth/refresh');
        const isMeRequest = originalRequest.url.includes('/api/v1/auth/me');

        if (status === 401 && !originalRequest._retry && !isLoginRequest && !isRefreshRequest) {
            originalRequest._retry = true;
            try {
                await axios.post(
                    `${process.env.NEXT_PUBLIC_API_URL}/api/v1/auth/refresh`,
                    {},
                    { withCredentials: true }
                );
                return api(originalRequest);
            } catch (refreshError) {
                if (isMeRequest) {
                    return Promise.resolve({ data: null });
                }
                return Promise.reject(refreshError);
            }
        }

        if (status === 401 && isMeRequest) {
            return Promise.resolve({ data: null });
        }

        if (status === 401 && isLoginRequest) {
            return Promise.reject(error);
        }

        if (status === 403 && !isMeRequest) {
            toast.error("FORBIDDEN ACCESS");
        }

        return Promise.reject(error);
    }
);