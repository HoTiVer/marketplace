
import {OrderRequest, OrderResponse, SellerOrdersResponse, UserOrdersResponse} from "@/types/order.types";
import {api} from "@/lib/api";

export const orderService = {

    createOrder: async (orderData: OrderRequest): Promise<OrderResponse> => {
        const { data } = await api.post<OrderResponse>('/api/v1/order', orderData);
        return data;
    },

    getUserOrders: async (page = 0, size = 10): Promise<UserOrdersResponse> => {
        const { data } = await api.get<UserOrdersResponse>('/api/v1/order/orders', {
            params: { page, size }
        });
        return data;
    },

    cancelOrder: async (orderId: number): Promise<void> => {
        await api.patch(`/api/v1/order/orders/${orderId}/cancel`);
    },

    getSellerOrders: async (page = 0, size = 10): Promise<SellerOrdersResponse> => {
        const { data } = await api.get<SellerOrdersResponse>('/api/v1/order/seller/manage-orders', {
            params: { page, size }
        });
        return data;
    },

    updateOrderStatus: async (orderId: number, status: string): Promise<void> => {
        await api.patch(`/api/v1/order/seller/manage-orders/${orderId}`, { status });
    }
};