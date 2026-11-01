export interface OrderRequest {
    receiverName: string;
    receiverPhone: string;
    deliveryCity: string;
    deliveryAddress: string;
}

export interface OrderResponse {
    id: number;
    status: string;
    totalPrice: number;
    createdAt: string;
}

export interface OrderItem {
    orderId: number;
    productId: number;
    productName: string;
    sellerNickname: string;
    quantity: number;
    orderDate: string;
    deliveryDate: string;
    orderStatus: string;
    totalPrice: number;
    deliveryAddress: string;
}

export interface UserOrdersResponse {
    content: OrderItem[];
    totalPages: number;
    totalElements: number;
    last: boolean;
    first: boolean;
    number: number;
    numberOfElements: number;
    empty: boolean;
}

export interface SellerOrder {
    orderId: number;
    productId: number;
    productName: string;
    quantity: number;
    orderDate: string;
    deliveryDate: string;
    orderStatus: string;
    totalPrice: number;
    recipientName: string;
    recipientPhone: string;
    deliveryAddress: string;
    deliveryCity: string;
}

export interface SellerOrdersResponse {
    orders: {
        content: SellerOrder[];
        totalPages: number;
        totalElements: number;
        number: number;
        last: boolean;
    };
    statuses: string[];
}

export interface OrderRequest {
    receiverName: string;
    receiverPhone: string;
    deliveryCity: string;
    deliveryAddress: string;
    paymentMethod: 'card' | 'cash';
}