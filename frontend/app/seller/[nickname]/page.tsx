'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams } from 'next/navigation';
import { useAuth } from "@/context/AuthContext";
import { sellerService } from "@/services/sellerService";
import { SellerType } from "@/types/seller.types";
import Link from 'next/link';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from "axios";

export default function SellerProfilePage() {
    const params = useParams();
    const nickname = typeof params?.nickname === 'string' ? params.nickname : '';
    const { user, isLoading: authLoading } = useAuth();

    const [seller, setSeller] = useState<SellerType | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [message, setMessage] = useState('');
    const [messageStatus, setMessageStatus] = useState<{type: 'success' | 'error', text: string} | null>(null);
    const [isSending, setIsSending] = useState(false);

    const stompClient = useRef<Client | null>(null);

    useEffect(() => {
        if (!user) return;

        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            console.log('STOMP: Connected');
        };

        client.onStompError = (frame) => {
            console.error('STOMP: Error', frame.headers['message']);
        };

        client.activate();
        stompClient.current = client;

        return () => {
            if (stompClient.current) {
                stompClient.current.deactivate();
            }
        };
    }, [user]);

    const loadSeller = useCallback(async (name: string) => {
        try {
            setIsLoading(true);
            const data = await sellerService.getByNickname(name);
            setSeller(data);
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err))
                setError(err.response?.data?.message ?? 'Connection unavailable');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        if (nickname) loadSeller(nickname);
    }, [nickname, loadSeller]);

    const handleSendMessage = () => {
        if (!message.trim() || isSending || !seller) return;

        if (!stompClient.current || !stompClient.current.connected) {
            setMessageStatus({ type: 'error', text: 'Connection not established' });
            return;
        }

        setIsSending(true);
        setMessageStatus(null);

        try {
            const sendMessageDto = {
                receiverId: seller.id,
                content: message
            };

            stompClient.current.publish({
                destination: '/app/chat.sent',
                body: JSON.stringify(sendMessageDto),
            });

            setMessageStatus({ type: 'success', text: 'Message transmitted via secure channel' });
            setMessage('');
        } catch (err) {
            console.error(err);
            setMessageStatus({ type: 'error', text: 'Transmission failed' });
        } finally {
            setIsSending(false);
        }
    };

    if (isLoading && !error) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-[#fafafa]">
                <div className="h-10 w-10 border-4 border-blue-600/10 border-t-blue-600 rounded-full animate-spin"></div>
            </div>
        );
    }

    if (error || !seller) {
        return (
            <div className="min-h-screen flex flex-col items-center justify-center p-6 text-center bg-[#fafafa]">
                <div className="bg-white p-12 rounded-[2.5rem] border border-gray-100 shadow-xl max-w-md w-full">
                    <p className="text-[10px] font-black uppercase tracking-[0.4em] text-gray-300 mb-8 leading-relaxed">
                        {error || "Merchant profile not found in system"}
                    </p>
                    <Link href="/" className="inline-block w-full py-5 bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest rounded-2xl hover:bg-blue-600 transition-all shadow-lg shadow-gray-900/10">
                        Return to Index
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#fafafa] selection:bg-blue-600 selection:text-white">
            <main className="max-w-6xl mx-auto pt-24 p-6 pb-20">

                <div className="relative bg-white rounded-[3.5rem] border border-blue-100 shadow-[0_30px_70px_rgba(37,99,235,0.04)] overflow-hidden">

                    <div className="absolute -right-20 -top-20 w-96 h-96 bg-blue-50/40 rounded-full blur-[100px] pointer-events-none" />
                    <div className="absolute -left-20 -bottom-20 w-80 h-80 bg-blue-50/30 rounded-full blur-[80px] pointer-events-none" />

                    <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-0">

                        <div className="lg:col-span-7 p-10 md:p-16 border-b lg:border-b-0 lg:border-r border-blue-50">
                            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-8 mb-12">
                                <div className="w-24 h-24 bg-gray-900 text-white rounded-[2.5rem] flex items-center justify-center text-4xl font-black shadow-2xl shadow-gray-900/20 border-4 border-white">
                                    {seller.displayName[0]}
                                </div>
                                <div>
                                    <div className="flex items-center gap-3 mb-3">
                                        <span className="px-4 py-1.5 bg-blue-600 text-white text-[9px] font-black uppercase tracking-[0.2em] rounded-lg shadow-lg shadow-blue-200">Verified Merchant</span>
                                        <span className="text-[10px] font-bold text-gray-300 uppercase tracking-widest">v2.4</span>
                                    </div>
                                    <h1 className="text-5xl font-black tracking-[-0.05em] text-gray-900 uppercase leading-none">
                                        {seller.displayName}
                                    </h1>
                                    <p className="text-[11px] font-bold text-gray-400 uppercase tracking-[0.3em] mt-3">Registry ID: {seller.nickname}</p>
                                </div>
                            </div>

                            <div className="bg-gray-50/50 border border-gray-100 p-8 rounded-[2.5rem] mb-12">
                                <p className="text-sm font-bold text-gray-500 uppercase tracking-wide leading-relaxed">
                                    {seller.profileDescription || "No description provided by the merchant."}
                                </p>
                            </div>

                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-10">
                                <div className="p-8 bg-white border border-blue-50 rounded-[2rem] shadow-sm">
                                    <span className="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em] block mb-4">Rating Score</span>
                                    <div className="flex items-end gap-3">
                                        <span className="text-4xl font-black text-gray-900 leading-none">{seller.rating.toFixed(1)}</span>
                                        <div className="flex gap-1 mb-1.5">
                                            {[...Array(5)].map((_, i) => (
                                                <div key={i} className={`w-2 h-2 rounded-full ${i < Math.floor(seller.rating) ? 'bg-blue-600' : 'bg-gray-100'}`} />
                                            ))}
                                        </div>
                                    </div>
                                </div>
                                <div className="p-8 bg-white border border-blue-50 rounded-[2rem] shadow-sm">
                                    <span className="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em] block mb-4">Merchant Status</span>
                                    <div className="flex items-center gap-3 text-emerald-500">
                                        <span className="w-3 h-3 bg-emerald-500 rounded-full animate-pulse shadow-lg shadow-emerald-200" />
                                        <span className="text-sm font-black uppercase tracking-widest">Operational</span>
                                    </div>
                                </div>
                            </div>

                            <Link
                                href={`/seller/${seller.nickname}/products`}
                                className="group w-full py-6 bg-gray-900 text-white text-[11px] font-black uppercase tracking-[0.3em] rounded-[1.5rem] flex items-center justify-center gap-4 hover:bg-blue-600 transition-all shadow-xl shadow-gray-900/10 active:scale-[0.98]"
                            >
                                View Merchant Inventory
                                <span className="group-hover:translate-x-2 transition-transform duration-500">→</span>
                            </Link>
                        </div>

                        <div className="lg:col-span-5 p-10 md:p-16 bg-[#fcfcfc]">
                            <div className="h-full flex flex-col justify-center">
                                <div className="mb-10 text-center lg:text-left">
                                    <h3 className="text-[10px] font-black uppercase tracking-[0.5em] text-gray-400 mb-2">Internal Channel</h3>
                                    <p className="text-2xl font-black tracking-tight text-gray-900 uppercase">Direct Inquiry</p>
                                </div>

                                {authLoading ? (
                                    <div className="py-20 flex justify-center"><div className="h-6 w-6 border-2 border-blue-600/10 border-t-blue-600 rounded-full animate-spin" /></div>
                                ) : !user ? (
                                    <div className="bg-white border border-blue-50 p-10 rounded-[2.5rem] text-center shadow-sm">
                                        <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest leading-relaxed mb-8">
                                            Authentication required to establish connection with the merchant.
                                        </p>
                                        <Link href="/auth/login" className="block w-full py-4 border-2 border-gray-900 text-gray-900 text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-gray-900 hover:text-white transition-all">
                                            Authorize
                                        </Link>
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        <textarea
                                            value={message}
                                            onChange={(e) => setMessage(e.target.value)}
                                            placeholder="Specify your inquiry details..."
                                            rows={6}
                                            /* Убрал uppercase, увеличил контраст и размер шрифта */
                                            className="w-full bg-white border border-blue-100 rounded-[2rem] p-8 text-base font-medium text-gray-900 placeholder:text-gray-300 focus:ring-4 focus:ring-blue-600/5 focus:border-blue-300 outline-none transition-all resize-none shadow-[inner_0_2px_4px_rgba(0,0,0,0.02)] tracking-normal"
                                        />

                                        {messageStatus && (
                                            <div className={`p-4 rounded-xl text-center text-[9px] font-black uppercase tracking-widest shadow-sm ${
                                                messageStatus.type === 'success' ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-red-50 text-red-500 border border-red-100'
                                            }`}>
                                                {messageStatus.text}
                                            </div>
                                        )}

                                        <button
                                            onClick={handleSendMessage}
                                            disabled={!message.trim() || isSending}
                                            className="w-full py-6 bg-blue-600 text-white text-[11px] font-black uppercase tracking-[0.3em] rounded-[1.5rem] disabled:opacity-20 transition-all hover:bg-gray-900 shadow-xl shadow-blue-200 active:scale-95"
                                        >
                                            {isSending ? 'Transmitting...' : 'Establish Contact'}
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    <div className="absolute left-0 top-0 bottom-0 w-2 bg-blue-600 opacity-10" />
                </div>

            </main>
        </div>
    );
}