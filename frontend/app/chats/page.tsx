/* eslint-disable @typescript-eslint/no-explicit-any */

"use client";

import React, { useState, useEffect, useRef, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UpdateChatEvent, ChatSummary, ChatFull } from '@/types/chat.types';
import { toast } from 'sonner';
import { chatService } from "@/services/chatService";

const formatChatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    if (isToday) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString([], { day: 'numeric', month: 'short' });
};

function ChatContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const chatIdFromUrl = searchParams.get('id');

    const [chats, setChats] = useState<ChatSummary[]>([]);
    const [selectedChat, setSelectedChat] = useState<ChatFull | null>(null);
    const [messageText, setMessageText] = useState("");
    const [isConnected, setIsConnected] = useState(false);

    const stompClient = useRef<Client | null>(null);
    const scrollRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    const scrollToBottom = (behavior: ScrollBehavior = 'smooth') => {
        if (scrollRef.current) {
            setTimeout(() => {
                scrollRef.current?.scrollTo({
                    top: scrollRef.current.scrollHeight,
                    behavior: behavior
                });
            }, 0);
        }
    };

    useEffect(() => {
        if (selectedChat) {
            scrollToBottom(selectedChat.messages.length > 10 ? 'auto' : 'smooth');
            inputRef.current?.focus();
        }
    }, [selectedChat?.chatId, selectedChat?.messages]);

    useEffect(() => {
        loadInitialData();
        connectWebSocket();
        return () => {
            if (stompClient.current) stompClient.current.deactivate();
        };
    }, []);

    useEffect(() => {
        if (chatIdFromUrl) {
            loadFullChat(Number(chatIdFromUrl));
        }
    }, [chatIdFromUrl]);

    const loadInitialData = async () => {
        try {
            const data = await chatService.getChats();
            const sortedChats = data.sort((a:any, b:any) =>
                new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
            );
            setChats(sortedChats);
        } catch (err) {
            console.error("Initial load failed", err);
        }
    };

    const loadFullChat = async (id: number) => {
        try {
            const data = await chatService.getChatById(id);
            setSelectedChat(data);
        } catch (err) {
            console.error("Failed to load chat", err);
        }
    };

    const handleChatSelection = (id: number) => {
        router.push(`/chats?id=${id}`, { scroll: false });
    };

    const handleProfileNavigation = () => {
        if (selectedChat?.isSeller && selectedChat?.sellerUsername) {
            router.push(`/seller/${selectedChat.sellerUsername}`);
        }
    };

    const connectWebSocket = () => {
        const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_URL}/ws`);
        const client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                setIsConnected(true);
                client.subscribe('/user/queue/chats', (payload) => {
                    const event: UpdateChatEvent = JSON.parse(payload.body);
                    handleIncomingMessage(event);
                });
            },
            onDisconnect: () => setIsConnected(false),
            onStompError: (frame) => {
                console.error('STOMP error', frame);
                toast.error("Connection error");
            }
        });
        client.activate();
        stompClient.current = client;
    };

    const handleIncomingMessage = (event: UpdateChatEvent) => {
        setSelectedChat((prev) => {
            if (prev && prev.chatId === event.chatId) {
                const exists = prev.messages.some(m => m.id === event.messageId);
                if (exists) return prev;
                return {
                    ...prev,
                    messages: [...prev.messages, {
                        id: event.messageId,
                        senderId: event.senderId,
                        senderName: "",
                        content: event.content,
                        sentAt: event.sentAt.toString(),
                    }]
                };
            }
            return prev;
        });

        setChats((prevChats) => {
            const chatIndex = prevChats.findIndex(c => c.chatId === event.chatId);

            if (chatIndex === -1) {
                loadInitialData();
                return prevChats;
            }

            const updatedChats = [...prevChats];
            const updatedChat = {
                ...updatedChats[chatIndex],
                lastMessage: event.content,
                updatedAt: event.sentAt.toString()
            };

            updatedChats.splice(chatIndex, 1);
            return [updatedChat, ...updatedChats];
        });
    };

    const handleSend = () => {
        if (!messageText.trim() || !selectedChat) return;
        if (stompClient.current?.connected) {
            const sendMessageDto = {
                receiverId: selectedChat.receiverId,
                content: messageText
            };
            stompClient.current.publish({
                destination: '/app/chat.sent',
                body: JSON.stringify(sendMessageDto)
            });
            setMessageText("");
            scrollToBottom('smooth');
        } else {
            toast.error("Chat disconnected");
        }
    };

    return (
        <div className="flex h-screen pt-[64px] bg-[#F8F9FA] overflow-hidden text-black font-sans">
            <aside className="w-80 bg-white border-r border-slate-100 flex flex-col m-2 rounded-2xl shadow-sm overflow-hidden">
                <div className="p-6 flex-shrink-0">
                    <h1 className="text-2xl font-bold tracking-tight">Messages</h1>
                </div>
                <div className="flex-1 overflow-y-auto px-3 pb-4 custom-scrollbar">
                    {chats.map((chat) => (
                        <div
                            key={chat.chatId}
                            onClick={() => handleChatSelection(chat.chatId)}
                            className={`group p-4 mb-2 cursor-pointer rounded-xl transition-all duration-200 flex items-center gap-4 ${
                                selectedChat?.chatId === chat.chatId
                                    ? 'bg-black text-white shadow-md'
                                    : 'hover:bg-slate-50 text-black'
                            }`}
                        >
                            <div className={`w-11 h-11 flex-shrink-0 rounded-full flex items-center justify-center font-bold ${
                                selectedChat?.chatId === chat.chatId ? 'bg-white/20' : 'bg-slate-100 text-slate-600'
                            }`}>
                                {chat.name[0].toUpperCase()}
                            </div>
                            <div className="flex-1 min-w-0">
                                <div className="flex justify-between items-baseline mb-0.5">
                                    <p className="font-semibold truncate text-sm mr-2">{chat.name}</p>
                                    {/* ВЫВОД ВРЕМЕНИ ОБНОВЛЕНИЯ */}
                                    <span className={`text-[10px] font-medium flex-shrink-0 ${
                                        selectedChat?.chatId === chat.chatId ? 'text-slate-400' : 'text-slate-400'
                                    }`}>
                                        {formatChatDate(chat.updatedAt)}
                                    </span>
                                </div>
                                {chat.lastMessage && (
                                    <p className={`text-xs truncate ${
                                        selectedChat?.chatId === chat.chatId ? 'text-slate-300' : 'text-slate-400'
                                    }`}>
                                        {chat.lastMessage}
                                    </p>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </aside>

            <main className="flex-1 flex flex-col bg-white m-2 ml-0 rounded-2xl shadow-sm overflow-hidden relative">
                {selectedChat ? (
                    <>
                        {/* Header Content... (без изменений) */}
                        <div className="px-6 py-4 border-b border-slate-50 flex justify-between items-center bg-white/80 backdrop-blur-md z-10">
                            <div
                                className={`flex items-center gap-4 ${selectedChat.isSeller ? 'cursor-pointer group/header' : ''}`}
                                onClick={handleProfileNavigation}
                            >
                                <div className="w-10 h-10 bg-slate-900 rounded-full flex items-center justify-center text-white font-bold group-hover/header:scale-105 transition-transform">
                                    {selectedChat.chatName[0].toUpperCase()}
                                </div>
                                <div>
                                    <h2 className={`font-bold text-base leading-none ${selectedChat.isSeller ? 'group-hover/header:text-blue-600 transition-colors' : ''}`}>
                                        {selectedChat.chatName}
                                    </h2>
                                    {selectedChat.isSeller && (
                                        <div className="flex items-center gap-1 mt-1">
                                            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Verified Seller</span>
                                            <svg className="w-3 h-3 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                            </svg>
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="flex items-center gap-3 bg-slate-50 px-3 py-1.5 rounded-full">
                                <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`} />
                                <span className="text-[10px] font-bold text-slate-600 uppercase">{isConnected ? 'Live' : 'Offline'}</span>
                            </div>
                        </div>

                        {/* Feed Content... */}
                        <div ref={scrollRef} className="flex-1 p-6 overflow-y-auto space-y-4 bg-white custom-scrollbar scroll-smooth">
                            {selectedChat.messages.map((msg, idx) => {
                                const isMe = msg.senderId !== selectedChat.receiverId;
                                return (
                                    <div key={idx} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                                        <div className={`max-w-[70%] px-5 py-3 transition-all ${
                                            isMe
                                                ? 'bg-black text-white rounded-2xl rounded-tr-none shadow-sm'
                                                : 'bg-slate-100 text-black rounded-2xl rounded-tl-none'
                                        }`}>
                                            <p className="text-sm leading-relaxed">{msg.content}</p>
                                            <p className={`text-[10px] mt-2 font-medium opacity-50 ${isMe ? 'text-right' : 'text-left'}`}>
                                                {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                            </p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        {/* Input Area... */}
                        <div className="p-4 bg-white border-t border-slate-50">
                            <div className="relative flex items-center gap-3 p-1.5 bg-slate-50 rounded-xl border border-slate-100 focus-within:border-slate-300 focus-within:bg-white transition-all">
                                <input
                                    ref={inputRef}
                                    type="text"
                                    value={messageText}
                                    onChange={(e) => setMessageText(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                                    placeholder="Write a message..."
                                    className="flex-1 bg-transparent px-4 py-2 outline-none text-sm text-black placeholder:text-slate-400"
                                />
                                <button
                                    onClick={handleSend}
                                    disabled={!messageText.trim()}
                                    className="bg-black text-white p-2.5 rounded-lg hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-20"
                                >
                                    <svg fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-5 h-5">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-center p-10">
                        <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
                            <svg fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-8 h-8 text-slate-300">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M7.5 8.25h9m-9 3h9m-9 3h9m-6.75-12.75L3 5.25v16.5L7.5 17.25h9a2.25 2.25 0 002.25-2.25V5.25a2.25 2.25 0 00-2.25-2.25h-9z" />
                            </svg>
                        </div>
                        <h3 className="text-lg font-bold text-black">Select a chat</h3>
                    </div>
                )}
            </main>
        </div>
    );
}

export default function ChatPage() {
    return (
        <Suspense fallback={<div className="flex h-screen items-center justify-center">Loading...</div>}>
            <ChatContent />
        </Suspense>
    );
}