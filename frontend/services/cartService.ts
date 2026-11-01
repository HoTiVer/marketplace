import { api } from "@/lib/api";
import {CartItem} from "@/types/cart.types";
import {OrderRequest} from "@/types/order.types";

export const cartService = {

    async getCart(): Promise<CartItem[]> {
        const { data } = await api.get("/api/v1/cart");
        return data;
    },

    async addToCart(productId: number | string): Promise<void> {
        await api.post(`/api/v1/cart/${productId}`);
    },

    async updateCount(productId: number, count: number): Promise<void> {
        await api.patch(`/api/v1/cart/${productId}`, null, {
            params: { count }
        });
    },

    async deleteItem(productId: number): Promise<void> {
        await api.delete(`/api/v1/cart/${productId}`);
    },

    async createOrder(orderData: OrderRequest): Promise<void> {
        await api.post("/api/v1/order", orderData);
    }
};