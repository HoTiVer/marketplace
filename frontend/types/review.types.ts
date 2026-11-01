export interface Review {
    reviewId: number;
    commentatorName: string;
    comment: string;
    rating: number;
    updatedAt: string;
}

export interface ProductReviewResponse {
    productId: number;
    productName: string;
    rating: number;
    productReviews: Review[];
}

export interface CreateReviewRequest {
    rating: number;
    comment?: string;
}