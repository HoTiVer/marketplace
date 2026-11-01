import { api } from "@/lib/api";
import {
    PriceHistoryItem,
    ProductAddTypes,
    ProductSaveRequest,
    ProductSummary,
    ProductTypes, Promotion, PromotionPayload
} from "@/types/product.types";
import axios from "axios";
import {PageResponse} from "@/types/response.types";
import {ApiError} from "@/types/apiError.types";

export const productService = {

    handleApiError(error: unknown): string {
        if (axios.isAxiosError(error) && error.response) {
            const data = error.response.data as ApiError;

            if (data.errors) {
                return Object.values(data.errors).flat()[0];
            }

            if (data.message) return data.message;
        }
        return "System failure. Try again later.";
    },

    async getProductById(id: string): Promise<ProductTypes | { message: string } | null> {
        try {
            const {data} = await api.get<ProductTypes>(`/api/v1/product/${id}`);
            return data;
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                return {
                    message: error.response.data?.message || "Product not found"
                };
            }

            return {message: "An unexpected error occurred"};
        }
    },

    async searchProducts(searchTerm: string, page: number = 0, size: number = 8): Promise<PageResponse<ProductSummary>> {
        const {data} = await api.get(`/api/v1/search/product`, {
            params: {searchTerm, page, size}
        });
        return data;
    },

    async getByCategory(categoryId: string, page: number = 0, size: number = 8): Promise<PageResponse<ProductSummary>> {
        const {data} = await api.get(`/api/v1/search/product/category/${categoryId}`, {
            params: {page, size}
        });
        return data;
    },

    async getSellerProducts(): Promise<ProductTypes[]> {
        const { data } = await api.get('/api/v1/seller/products');
        return data;
    },

    async getSellerProductById(id: number): Promise<ProductAddTypes> {
        const { data } = await api.get(`/api/v1/seller/products/${id}`);
        return data;
    },

    async saveProduct(id: number | string | null, productData: ProductSaveRequest, imageFile?: File) {
        const formData = new FormData();
        console.log("Sending to back:", productData);

        const jsonBlob = new Blob([JSON.stringify(productData)], {
            type: "application/json"
        });

        formData.append("data", jsonBlob);

        if (imageFile) {
            formData.append("image", imageFile);
        }

        if (id) {
            return await api.put(`/api/v1/product/${id}`, formData);
        }
        return await api.post('/api/v1/product', formData);
    },

    async deleteProduct(id: number) {
        return await api.delete(`/api/v1/product/${id}`);
    },

    async setMainImage(productId: number | string, imageId: number | string): Promise<void> {
        try {
            await api.patch(`/api/v1/product/${productId}/image/${imageId}/main`);
        } catch (error) {
            console.error("Failed to set main image:", error);
            throw error;
        }
    },

    async deleteProductImage(productId: number | string, imageId: number | string): Promise<void> {
        try {
            await api.delete(`/api/v1/product/${productId}/image/${imageId}`);
        } catch (error) {
            console.error("Failed to delete image:", error);
            throw error;
        }
    },

    async getPriceHistory(productId: number | string): Promise<PriceHistoryItem[]> {
        const { data } = await api.get<PriceHistoryItem[]>(`/api/v1/seller/products/${productId}/price-history`);
        return data;
    },

    async getPromotions(productId: number | string): Promise<Promotion[]> {
        const { data } = await api.get<Promotion[]>(`/api/v1/products/${productId}/promotions`);
        return data;
    },

    async savePromotion(productId: number | string, promoData: PromotionPayload): Promise<Promotion> {
        const { data } = await api.post<Promotion>(`/api/v1/products/${productId}/promotions`, promoData);
        return data;
    },

    async updatePromotion(promotionId: number, promoData: PromotionPayload): Promise<Promotion> {
        const { data } = await api.put<Promotion>(`/api/v1/products/promotions/${promotionId}`, promoData);
        return data;
    },

    async deletePromotion(promotionId: number): Promise<void> {
        await api.delete(`/api/v1/products/promotions/${promotionId}`);
    },
}