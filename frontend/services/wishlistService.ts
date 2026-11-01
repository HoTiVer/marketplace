import { api } from "@/lib/api";
import { ProductSummary } from "@/types/product.types";


export const wishlistService = {
    async getWishlist(): Promise<ProductSummary[]> {
        const response = await api.get("/api/v1/wishlist");
        return response.data;
    },

    async addToWishlist(productId: number): Promise<void> {
        await api.post(`/api/v1/wishlist/${productId}`);
    },

    async removeFromWishlist(productId: number): Promise<void> {
        await api.delete(`/api/v1/wishlist/${productId}`);
    }
};