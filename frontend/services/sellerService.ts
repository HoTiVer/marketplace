import { api } from "@/lib/api";
import {SellerType} from "@/types/seller.types";
import {ProductSummary} from "@/types/product.types";

export interface SellerRegisterDTO {
    requestedNickname: string;
    displayName: string;
    description: string;
}

export const sellerService = {
    async getByNickname(nickname: string): Promise<SellerType> {
        const { data } = await api.get(`/api/v1/seller/${nickname}`);
        return data;
    },


    async sendMessage(nickname: string, content: string): Promise<void> {
        await api.post(`/api/v1/seller/message/${nickname}`, { content });
    },

    async getProductsByNickname(nickname: string): Promise<ProductSummary[]> {
        const { data } = await api.get(`/api/v1/seller/${nickname}/products`);
        return data;
    },

    async register(payload: SellerRegisterDTO): Promise<void> {
        await api.post('/api/v1/cabinet/new-seller/register', payload);
    }
};