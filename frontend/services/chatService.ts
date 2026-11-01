import {api} from "@/lib/api";
import {ChatFull, ChatSummary} from "@/types/chat.types";


export const chatService = {
    getChats: () => api.get<ChatSummary[]>('/api/v1/chat').then(r => r.data),
    getChatById: (id: number) => api.get<ChatFull>(`/api/v1/chat/${id}`).then(r => r.data),
    sendMessage: (id: number, content: string) => api.post(`/api/v1/chat/${id}/message`, { content })
};