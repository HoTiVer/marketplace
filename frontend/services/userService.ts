
import {ChangePasswordRequest, PersonalInfo} from "@/types/user.types";
import {api} from "@/lib/api";

export const userService = {
    getPersonalInfo: async (): Promise<PersonalInfo> => {
        const response = await api.get('/api/v1/cabinet/personal-info');
        return response.data;
    },

    async changePassword(payload: ChangePasswordRequest): Promise<void> {
        await api.put('/api/v1/auth/security/password', payload);
    },

    async toggle2FA(enabled: boolean): Promise<void> {
        await api.post('/api/v1/auth/security/2fa', { enabled });
    }
};