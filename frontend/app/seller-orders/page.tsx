'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import Link from 'next/link';
import { orderService } from "@/services/orderService";
import { SellerOrder, SellerOrdersResponse } from "@/types/order.types";

const STATUS_MAP: Record<string, string[]> = {
    CREATED: ['CONFIRMED', 'CANCELLED'],
    CONFIRMED: ['IN_TRANSIT', 'CANCELLED'],
    IN_TRANSIT: ['DELIVERED', 'RETURNED'],
    DELIVERED: ['COMPLETED', 'RETURNED'],
    CANCELLED: [],
    RETURNED: [],
    COMPLETED: []
};

export default function SellerOrdersPage() {
    const [data, setData] = useState<SellerOrdersResponse | null>(null);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [toast, setToast] = useState<string | null>(null);

    const loadOrders = useCallback(async (pageNum: number = 0) => {
        setIsLoading(true);
        try {
            const res = await orderService.getSellerOrders(pageNum, 10);
            setData(res);
        } catch (err) {
            console.error("Logistics sync failed");
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        loadOrders(page);
    }, [page, loadOrders]);

    const showToast = (msg: string) => {
        setToast(msg);
        setTimeout(() => setToast(null), 3000);
    };

    const handleStatusChange = async (orderId: number, newStatus: string) => {
        try {
            await orderService.updateOrderStatus(orderId, newStatus);
            showToast(`Protocol Updated: #${orderId} -> ${newStatus}`);
            loadOrders(page);
        } catch (err) {
            showToast("System error: Update rejected.");
        }
    };

    return (
        <div className="min-h-screen bg-[#fafafa] selection:bg-blue-600 selection:text-white pt-24 p-6 pb-32">
            <main className="max-w-7xl mx-auto">

                {/* Header */}
                <header className="mb-12 border-b-2 border-gray-900 pb-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
                    <div>
                        <h1 className="text-6xl font-black tracking-[-0.05em] text-gray-900 uppercase">
                            Seller<span className="text-blue-600">.</span>Hub
                        </h1>
                        <p className="text-[10px] font-black uppercase tracking-[0.5em] text-gray-400 mt-4">
                            Operational Management / {data?.orders.totalElements || 0} active units
                        </p>
                    </div>

                    <div className="flex items-center gap-2">
                        <button
                            disabled={page === 0 || isLoading}
                            onClick={() => setPage(p => p - 1)}
                            className="h-12 px-6 bg-white border border-gray-200 rounded-xl text-[10px] font-black uppercase tracking-widest disabled:opacity-20 hover:border-blue-600 transition-all active:scale-95"
                        >PREV</button>
                        <div className="h-12 px-6 flex items-center bg-gray-900 text-white rounded-xl text-[10px] font-black tracking-[0.2em]">
                            {page + 1} / {data?.orders.totalPages || 1}
                        </div>
                        <button
                            disabled={data?.orders.last || isLoading}
                            onClick={() => setPage(p => p + 1)}
                            className="h-12 px-6 bg-white border border-gray-200 rounded-xl text-[10px] font-black uppercase tracking-widest disabled:opacity-20 hover:border-blue-600 transition-all active:scale-95"
                        >NEXT</button>
                    </div>
                </header>

                {isLoading ? (
                    <div className="space-y-4">
                        {[1, 2, 3].map(i => <div key={i} className="h-48 w-full bg-white rounded-[2.5rem] animate-pulse border border-gray-100" />)}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-6">
                        {data?.orders.content.map((order) => (
                            <SellerOrderCard
                                key={order.orderId}
                                order={order}
                                availableStatuses={data.statuses}
                                onStatusChange={handleStatusChange}
                            />
                        ))}
                    </div>
                )}
            </main>

            {toast && (
                <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-[300] bg-gray-900 text-white px-8 py-5 rounded-2xl text-[10px] font-black uppercase tracking-[0.2em] animate-in slide-in-from-bottom-4 shadow-2xl border border-gray-700">
                    <span className="text-blue-400 mr-2">●</span> {toast}
                </div>
            )}
        </div>
    );
}

function SellerOrderCard({ order, availableStatuses, onStatusChange }: {
    order: SellerOrder,
    availableStatuses: string[],
    onStatusChange: (id: number, status: string) => void
}) {
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);
    const nextOptions = STATUS_MAP[order.orderStatus.toUpperCase()] || [];
    const canManage = nextOptions.length > 0;

    // Закрытие по клику вне меню
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <div className="group bg-white border border-gray-100 rounded-[2.5rem] p-8 shadow-sm hover:border-blue-200 transition-all duration-500">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">

                <div className="lg:col-span-5 space-y-4">
                    <div className="flex items-center gap-3">
                        <span className={`px-4 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest ${getStatusStyles(order.orderStatus)}`}>
                            {order.orderStatus}
                        </span>
                        <span className="text-[10px] font-bold text-gray-300 uppercase tracking-widest">
                            Unit #{order.orderId.toString().padStart(6, '0')}
                        </span>
                    </div>
                    <div>
                        <Link href={`/product/${order.productId}`} className="text-2xl font-black text-gray-900 uppercase tracking-tight hover:text-blue-600 leading-none block">
                            {order.productName}
                        </Link>
                        <div className="flex items-center gap-4 mt-3">
                            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic">
                                Logged: {new Date(order.orderDate).toLocaleDateString()}
                            </p>
                        </div>
                    </div>
                </div>

                <div className="lg:col-span-4 bg-gray-50/50 border border-gray-100 rounded-[2rem] p-6 space-y-4">
                    <p className="text-[9px] font-black text-gray-400 uppercase tracking-[0.3em]">Destination Data</p>
                    <div className="space-y-1">
                        <p className="text-sm font-black text-gray-900 uppercase leading-none">{order.recipientName}</p>
                        <p className="text-[11px] font-bold text-blue-600">{order.recipientPhone}</p>
                        <p className="text-[11px] font-bold text-gray-500 leading-relaxed uppercase mt-2">
                            {order.deliveryCity} <br /> {order.deliveryAddress}
                        </p>
                    </div>
                </div>

                <div className="lg:col-span-3 flex flex-col justify-between items-end">
                    <div className="text-right">
                        <p className="text-3xl font-black text-gray-900 tracking-tighter leading-none">${order.totalPrice.toLocaleString()}</p>
                        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mt-2">Quantity: {order.quantity}</p>
                    </div>

                    <div className="w-full relative mt-6" ref={menuRef}>
                        {canManage ? (
                            <>
                                <button
                                    onClick={() => setIsOpen(!isOpen)}
                                    className={`w-full h-14 rounded-2xl flex items-center justify-between px-6 transition-all border-2 ${
                                        isOpen ? 'bg-white border-blue-600 shadow-xl shadow-blue-100' : 'bg-gray-900 border-gray-900 text-white hover:bg-blue-600 hover:border-blue-600'
                                    }`}
                                >
                                    <span className={`text-[10px] font-black uppercase tracking-widest ${isOpen ? 'text-gray-900' : 'text-white'}`}>
                                        Set Status
                                    </span>
                                    <svg className={`w-4 h-4 transition-transform duration-300 ${isOpen ? 'rotate-180 text-blue-600' : 'text-white'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M19 9l-7 7-7-7" />
                                    </svg>
                                </button>

                                {isOpen && (
                                    <div className="absolute bottom-full left-0 w-full mb-2 bg-white border-2 border-gray-900 rounded-2xl shadow-2xl overflow-hidden z-50 animate-in fade-in slide-in-from-bottom-2 duration-200">
                                        <div className="p-2 bg-gray-50 border-b border-gray-100">
                                            <p className="text-[8px] font-black text-gray-400 uppercase tracking-widest ml-2">Available Actions</p>
                                        </div>
                                        {availableStatuses
                                            .filter(s => nextOptions.includes(s))
                                            .map((s) => (
                                                <button
                                                    key={s}
                                                    onClick={() => {
                                                        onStatusChange(order.orderId, s);
                                                        setIsOpen(false);
                                                    }}
                                                    className="w-full text-left px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-900 hover:bg-blue-600 hover:text-white transition-colors border-b last:border-0 border-gray-50"
                                                >
                                                    {s}
                                                </button>
                                            ))}
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="w-full py-4 text-center bg-gray-100 rounded-2xl border border-gray-200">
                                <span className="text-[9px] font-black text-gray-400 uppercase tracking-widest">Unit Finalized</span>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

function getStatusStyles(status: string) {
    const s = status.toUpperCase();
    if (['DELIVERED', 'COMPLETED'].includes(s)) return 'bg-emerald-50 text-emerald-600 border border-emerald-100';
    if (['CREATED', 'CONFIRMED'].includes(s)) return 'bg-blue-50 text-blue-600 border border-blue-100';
    if (['IN_TRANSIT'].includes(s)) return 'bg-amber-50 text-amber-600 border border-amber-100';
    return 'bg-gray-100 text-gray-500 border border-gray-200';
}