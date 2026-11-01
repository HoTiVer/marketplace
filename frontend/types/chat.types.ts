
export interface ChatSummary {
    chatId: number;
    name: string;
    lastMessage: string;
    updatedAt: string;
}


export interface ChatMessage {
    id: number;
    senderId: number;
    content: string;
    sentAt: string;
}

export interface ChatFull {
    chatId: number;
    chatName: string;
    receiverId: number;
    isSeller: boolean;
    sellerUsername: string;
    messages: ChatMessage[];
}

export interface UpdateChatEvent {
    chatId: number;
    messageId: number;
    senderId: number;
    content: string;
    status: MessageStatus;
    sentAt: string;
}

export enum MessageStatus {
    SENT = "SENT",
    DELIVERED = "DELIVERED",
    READ = "READ"
}