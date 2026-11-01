export interface ProductImage {
    id: number
    url: string
    isMain: boolean
}

export interface ProductTypes {
    id: number;
    name: string;
    price: number;
    description: string;
    categoryName: string;
    characteristic: Record<string, string>;
    sellerDisplayName: string;
    sellerUsername: string;
    images: ProductImage[];
    promotion?: {
        isPromotion: boolean;
        defaultPrice: number;
        promotionPrice: number;
        promotionEndTime: string;
    };
}

export interface ProductSummary {
    productId: number;
    productName: string;
    price: number;
    mainImageUrl: string;
    promotionValue: number | null;
}

export interface ProductImage {
    id: number;
    url: string;
    isMain: boolean;
}

export interface ProductAddTypes {
    id: number;
    name: string;
    price: number;
    description: string;
    categoryName: string;
    quantity: number;
    sellerDisplayName: string;
    mainImageUrl?: string;
    characteristics: Record<string, string>;
    images?: ProductImage[];
}

export interface PriceHistoryItem {
    productId: number;
    price: number;
    createdAt: string;
}

export interface Promotion {
    promotionId: number;
    productId: number;
    productName: string;
    title: string;
    discountPercent: number;
    startTime: string;
    endTime: string;
    active: boolean;
    showEndDate: boolean;
}

export interface PromotionPayload {
    title: string;
    discountPercent: number;
    startTime: string;
    endTime: string;
    showEndDate: boolean;
    active: boolean;
}

export type ProductSaveRequest = Omit<ProductAddTypes, 'id' | 'sellerDisplayName' | 'mainImageUrl' | 'images'>
