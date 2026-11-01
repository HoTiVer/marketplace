export interface RegisterPayload {
    email: string;
    password: string;
    displayName: string;
}

export interface LoginPayload {
    email: string;
    password: string;
}

export interface AuthError {
    message: string;
    field?: keyof RegisterPayload;
}

export interface ForgotPasswordPayload {
    email: string;
}

export interface ResetPasswordPayload {
    newPassword: string;
    resetToken: string;
}