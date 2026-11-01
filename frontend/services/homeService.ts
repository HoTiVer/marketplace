import { HomeApiResponse } from "@/types/home.types";
import {api} from "@/lib/api";

export const marketService = {
    async getHomeData(): Promise<HomeApiResponse> {
        const { data } = await api.get<HomeApiResponse>("/api/v1/home");
        return data;
    }
};