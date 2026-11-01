import { ProductReviewResponse, CreateReviewRequest } from "@/types/review.types";
import {api} from "@/lib/api";

export const reviewService = {
    async getProductReviews(productId: string): Promise<ProductReviewResponse> {
        const response = await api.get(`/api/v1/product/${productId}/review`);
        return response.data;
    },

    async createReview(productId: string, data: CreateReviewRequest): Promise<void> {
        await api.post(`/api/v1/product/${productId}/review`, data);
    }
};