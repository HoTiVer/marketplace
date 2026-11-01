export interface UserContextType {
    displayName: string;
    roles: string[];
}

export interface PersonalInfo {
    email: string;
    displayName: string;
    registerDate: string;
    isSeller: boolean;
    sellerNickname: string | null;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    code?: string;
}