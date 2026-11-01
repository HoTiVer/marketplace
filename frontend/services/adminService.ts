
import { SellerRegistrationRequest } from "@/types/seller.types";
import {api} from "@/lib/api";

const BASE_URL = '/api/v1/admin/request/seller-register';

export const adminService = {
    getSellerRequests: async (): Promise<SellerRegistrationRequest[]> => {
        const response = await api.get(BASE_URL);
        return response.data;
    },

    approveSeller: async (id: number): Promise<void> => {
        await api.post(`${BASE_URL}/${id}`);
    },

    rejectSeller: async (id: number): Promise<void> => {
        await api.delete(`${BASE_URL}/${id}`);
    }
};