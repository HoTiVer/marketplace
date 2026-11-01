'use client';

import { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { orderService } from "@/services/orderService";
import { OrderItem, UserOrdersResponse } from "@/types/order.types";

export default function MyOrdersPage() {
    const [data, setData] = useState<UserOrdersResponse | null>(null);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    const [terminatingId, setTerminatingId] = useState<number | null>(null);
    const [isActionLoading, setIsActionLoading] = useState(false);

    const loadOrders = useCallback(async (pageNum: number = 0) => {
        setIsLoading(true);
        try {
            const res = await orderService.getUserOrders(pageNum || 0, 10);
            setData(res);
        } catch (err) {
            console.error("Failed to fetch logistics data", err);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        loadOrders(page);
    }, [page, loadOrders]);

    const executeCancel = async () => {
        if (!terminatingId) return;
        setIsActionLoading(true);
        try {
            await orderService.cancelOrder(terminatingId);
            setTerminatingId(null);
            loadOrders(page);
        } catch (err) {
            console.error("Termination failed");
        } finally {
            setIsActionLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#fafafa] selection:bg-blue-600 selection:text-white">
            <main className="max-w-6xl mx-auto pt-24 p-6 pb-32">

                <header className="mb-12 border-b-2 border-gray-900 pb-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
                    <div>
                        <h1 className="text-6xl font-black tracking-[-0.05em] text-gray-900 uppercase leading-none">
                            Order<span className="text-blue-600">.</span>Log
                        </h1>
                        <p className="text-[10px] font-bold uppercase tracking-[0.5em] text-gray-400 mt-4">
                            Operational history / Total units: {data?.totalElements || 0}
                        </p>
                    </div>

                    <div className="flex items-center gap-2">
                        <button
                            disabled={page === 0 || isLoading}
                            onClick={() => setPage(p => p - 1)}
                            className="h-12 px-6 bg-white border border-gray-200 rounded-xl text-[10px] font-black uppercase tracking-widest disabled:opacity-20 hover:border-blue-600 transition-all"
                        >PREV</button>
                        <div className="h-12 px-6 flex items-center bg-gray-900 text-white rounded-xl text-[10px] font-black tracking-[0.2em]">
                            {page + 1} / {data?.totalPages || 1}
                        </div>
                        <button
                            disabled={data?.last || isLoading}
                            onClick={() => setPage(p => p + 1)}
                            className="h-12 px-6 bg-white border border-gray-200 rounded-xl text-[10px] font-black uppercase tracking-widest disabled:opacity-20 hover:border-blue-600 transition-all"
                        >NEXT</button>
                    </div>
                </header>

                {/* Orders List */}
                {isLoading ? (
                    <div className="space-y-4">
                        {[...Array(3)].map((_, i) => <OrderSkeleton key={i} />)}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-6">
                        {data?.content.map((order) => (
                            <OrderCard
                                key={order.orderId}
                                order={order}
                                onCancelInitiate={(id) => setTerminatingId(id)}
                            />
                        ))}
                    </div>
                )}
            </main>

            {terminatingId && (
                <div className="fixed inset-0 z-[200] flex items-center justify-center p-6 backdrop-blur-xl bg-gray-900/60 animate-in fade-in duration-300">
                    <div className="bg-white w-full max-w-md rounded-[3rem] p-12 shadow-2xl border border-blue-50 text-center relative overflow-hidden">
                        <div className="absolute top-0 left-0 w-full h-1 bg-red-500" />
                        <h2 className="text-2xl font-black uppercase tracking-tight text-gray-900 mb-4">Confirm Termination</h2>
                        <p className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.2em] leading-relaxed mb-10">
                            You are about to terminate operational record #{terminatingId.toString().padStart(6, '0')}. This action is irreversible.
                        </p>
                        <div className="flex flex-col gap-3">
                            <button
                                onClick={executeCancel}
                                disabled={isActionLoading}
                                className="w-full py-5 bg-red-500 text-white text-[11px] font-black uppercase tracking-widest rounded-2xl hover:bg-gray-900 transition-all shadow-xl shadow-red-200"
                            >
                                {isActionLoading ? 'Terminating...' : 'Confirm Action'}
                            </button>
                            <button
                                onClick={() => setTerminatingId(null)}
                                className="w-full py-5 bg-gray-50 text-gray-400 text-[11px] font-black uppercase tracking-widest rounded-2xl hover:bg-gray-100 transition-all"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

function OrderCard({ order, onCancelInitiate }: { order: OrderItem, onCancelInitiate: (id: number) => void }) {
    const [isExpanded, setIsExpanded] = useState(false);
    const canCancel = ['CREATED', 'CONFIRMED'].includes(order.orderStatus.toUpperCase());

    return (
        <div className="group bg-white border border-blue-50 rounded-[2.5rem] p-8 shadow-[0_15px_40px_rgba(37,99,235,0.02)] hover:border-blue-200 transition-all duration-500">
            <div className="flex flex-col lg:flex-row justify-between gap-8">
                <div className="flex-1 space-y-4">
                    <div className="flex flex-wrap items-center gap-4">
                        <span className={`px-4 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest ${getStatusStyles(order.orderStatus)}`}>
                            {order.orderStatus}
                        </span>
                        <span className="text-[10px] font-bold text-gray-300 uppercase tracking-widest">
                            ID: #{order.orderId.toString().padStart(6, '0')}
                        </span>
                    </div>

                    <div className="space-y-1">
                        <Link href={`/product/${order.productId}`} className="text-2xl font-black text-gray-900 uppercase tracking-tight hover:text-blue-600 transition-colors block leading-none">
                            {order.productName}
                        </Link>
                        <div className="flex items-center gap-2 text-[10px] font-bold text-gray-400 uppercase tracking-widest">
                            Merchant: <Link href={`/seller/${order.sellerNickname}`} className="text-gray-900 hover:text-blue-600 underline decoration-blue-200 underline-offset-4">{order.sellerNickname}</Link>
                        </div>
                    </div>
                </div>

                <div className="flex flex-wrap lg:flex-col justify-between lg:justify-center items-end gap-2 lg:min-w-[200px]">
                    <p className="text-3xl font-black text-gray-900 tracking-tighter">${order.totalPrice.toLocaleString()}</p>
                    <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Qty: {order.quantity} units</p>
                </div>

                <div className="flex lg:flex-col gap-3 justify-end">
                    <button
                        onClick={() => setIsExpanded(!isExpanded)}
                        className="px-8 py-4 bg-gray-50 text-gray-900 text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-gray-100 transition-all active:scale-95"
                    >
                        {isExpanded ? 'Hide' : 'Details'}
                    </button>
                    {canCancel && (
                        <button
                            onClick={() => onCancelInitiate(order.orderId)}
                            className="px-8 py-4 bg-red-50 text-red-500 text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-red-500 hover:text-white transition-all active:scale-95 shadow-sm"
                        >
                            Terminate
                        </button>
                    )}
                </div>
            </div>

            {isExpanded && (
                <div className="mt-8 pt-8 border-t border-dashed border-blue-50 grid grid-cols-1 md:grid-cols-3 gap-8 animate-in slide-in-from-top-4">
                    <DetailItem label="Order Date" value={new Date(order.orderDate).toLocaleString()} />
                    <DetailItem label="Logistics Target" value={order.deliveryAddress} />
                    <DetailItem label="Estimated Delivery" value={new Date(order.deliveryDate).toLocaleString()} />
                </div>
            )}
        </div>
    );
}

function DetailItem({ label, value }: { label: string, value: string }) {
    return (
        <div>
            <p className="text-[9px] font-black text-gray-300 uppercase tracking-[0.2em] mb-1">{label}</p>
            <p className="text-xs font-bold text-gray-600 uppercase">{value}</p>
        </div>
    );
}

function OrderSkeleton() {
    return (
        <div className="h-40 w-full bg-white border border-gray-100 rounded-[2.5rem] animate-pulse" />
    );
}

function getStatusStyles(status: string) {
    const s = status.toUpperCase();
    if (['DELIVERED', 'COMPLETED'].includes(s)) return 'bg-emerald-50 text-emerald-600 border border-emerald-100';
    if (['CREATED', 'CONFIRMED'].includes(s)) return 'bg-blue-50 text-blue-600 border border-blue-100';
    if (['IN_TRANSIT'].includes(s)) return 'bg-amber-50 text-amber-600 border border-amber-100';
    if (['CANCELLED', 'RETURNED'].includes(s)) return 'bg-gray-100 text-gray-500';
    return 'bg-gray-50 text-gray-400';
}