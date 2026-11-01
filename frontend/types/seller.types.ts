export interface SellerType {
    id: number;
    nickname: string;
    displayName: string;
    rating: number;
    profileDescription: string;
}

export interface SellerRegistrationRequest {
    id: number;
    requestedNickname: string;
    displayName: string;
    profileDescription: string;
}