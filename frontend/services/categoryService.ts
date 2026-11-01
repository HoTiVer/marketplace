import { api } from "@/lib/api";
import { CategoryType } from "@/types/category.types";

export const categoryService = {
    async getAll(): Promise<CategoryType[]> {
        const { data } = await api.get("/api/v1/category");
        return data;
    },

    async create(name: string): Promise<CategoryType> {
        const { data } = await api.post("/api/v1/category", { name });
        return data;
    },

    async update(id: number, name: string): Promise<CategoryType> {
        const { data } = await api.put(`/api/v1/category/${id}`, { name });
        return data;
    },

    async delete(id: number): Promise<void> {
        await api.delete(`/api/v1/category/${id}`);
    }
};