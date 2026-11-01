"use client";

import { useEffect, useState, useMemo } from "react";
import { cartService } from "@/services/cartService";
import { CartItem } from "@/types/cart.types";
import Link from "next/link";
import { X, ShoppingBag, ArrowRight, Loader2, AlertCircle, CheckCircle2 } from "lucide-react";

export default function CartPage() {
    const [items, setItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<{ msg: string; type: 'error' | 'success' } | null>(null);

    const showNotify = (msg: string, type: 'error' | 'success' = 'success') => {
        setNotification({ msg, type });
        setTimeout(() => setNotification(null), 3000);
    };

    useEffect(() => {
        cartService.getCart()
            .then(setItems)
            .catch(() => showNotify("Failed to load cart", "error"))
            .finally(() => setLoading(false));
    }, []);

    const total = useMemo(() => items.reduce((s, i) => s + i.price * i.quantity, 0), [items]);

    const handleUpdateQuantity = async (productId: number, newCount: number) => {
        if (newCount < 1) return;

        const previousItems = [...items];
        setItems(current => current.map(item =>
            item.productId === productId ? { ...item, quantity: newCount } : item
        ));

        try {
            await cartService.updateCount(productId, newCount);
        } catch (error) {
            setItems(previousItems);
            showNotify("Update failed", "error");
        }
    };

    const handleDelete = async (productId: number) => {
        const previousItems = [...items];
        setItems(current => current.filter(item => item.productId !== productId));

        try {
            await cartService.deleteItem(productId);
            showNotify("Item removed", "success");
        } catch (error: unknown) {
            setItems(previousItems);
            showNotify("Delete failed", "error");
        }
    };

    if (loading) return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
        </div>
    );

    return (
        <main className="bg-gray-50 min-h-screen p-4 md:p-8 pb-24 relative">
            {notification && (
                <div className="fixed top-10 left-1/2 -translate-x-1/2 z-3000 animate-in fade-in slide-in-from-top-5">
                    <div className={`flex items-center gap-3 px-6 py-4 rounded-2xl bg-white shadow-2xl border ${notification.type === 'error' ? 'border-red-100' : 'border-blue-50'}`}>
                        {notification.type === 'error' ?
                            <AlertCircle className="text-red-500" size={18}/> :
                            <CheckCircle2 className="text-blue-600" size={18}/>
                        }
                        <span className="text-sm font-bold text-gray-900 uppercase tracking-tight">{notification.msg}</span>
                    </div>
                </div>
            )}

            <div className="max-w-360 mx-auto space-y-8">
                <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                    <div className="space-y-2">
                        <h1 className="text-5xl font-black tracking-tighter text-gray-900">Your Bag</h1>
                        <p className="text-gray-400 font-bold uppercase tracking-widest text-xs">{items.length} positions total</p>
                    </div>
                    <Link href="/" className="text-sm font-bold text-gray-400 hover:text-blue-600 transition-colors uppercase tracking-widest">
                        ← Continue Shopping
                    </Link>
                </header>

                {items.length > 0 ? (
                    <div className="grid lg:grid-cols-12 gap-8 items-start">
                        <div className="lg:col-span-8 bg-white rounded-[2.5rem] border border-gray-100 shadow-sm overflow-hidden">
                            {items.map((item) => (
                                <div key={item.productId} className="p-8 border-b border-gray-50 last:border-0 flex flex-col sm:flex-row items-center justify-between gap-8 group">
                                    <div className="flex items-center gap-8 w-full sm:w-auto">
                                        <div className="w-24 h-24 bg-gray-50 rounded-2xl p-4 border border-gray-100 flex-shrink-0 group-hover:scale-105 transition-transform duration-500">
                                            <img src={item.mainImageUrl || '/placeholder.png'} className="w-full h-full object-contain" />
                                        </div>
                                        <div className="space-y-1">
                                            <Link href={`/product/${item.productId}`} className="text-xl font-black text-gray-900 hover:text-blue-600 transition-colors tracking-tight">
                                                {item.productName}
                                            </Link>
                                            <p className="text-sm font-bold text-blue-600 uppercase tracking-widest">${item.price.toLocaleString()}</p>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-10 w-full sm:w-auto justify-between sm:justify-end">
                                        <div className="flex items-center bg-gray-50 rounded-xl p-1 border border-gray-100">
                                            <button
                                                onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}
                                                className="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-white hover:text-blue-600 transition-all text-gray-400 font-black"
                                            >—</button>
                                            <span className="w-10 text-center text-sm font-black text-gray-900">{item.quantity}</span>
                                            <button
                                                onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}
                                                className="w-10 h-10 flex items-center justify-center rounded-lg hover:bg-white hover:text-blue-600 transition-all text-gray-400 font-black"
                                            >+</button>
                                        </div>

                                        <div className="flex items-center gap-6">
                                            <p className="text-2xl font-black tracking-tighter text-gray-900 w-24 text-right">
                                                ${(item.price * item.quantity).toLocaleString()}
                                            </p>
                                            <button
                                                onClick={() => handleDelete(item.productId)}
                                                className="w-10 h-10 rounded-xl flex items-center justify-center text-gray-300 hover:text-red-500 hover:bg-red-50 transition-all"
                                            >
                                                <X size={20} strokeWidth={3} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="lg:col-span-4 lg:sticky lg:top-8">
                            <div className="bg-white p-10 rounded-[2.5rem] border border-gray-100 shadow-xl shadow-gray-200/40">
                                <h3 className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400 mb-8">Settlement</h3>
                                <div className="space-y-4 mb-10">
                                    <div className="flex justify-between items-baseline pt-4 border-t border-gray-50">
                                        <span className="text-lg font-black text-gray-900 tracking-tight">Total</span>
                                        <span className="text-4xl font-black tracking-tighter text-blue-600">
                                            ${total.toLocaleString()}
                                        </span>
                                    </div>
                                </div>
                                <Link
                                    href="/checkout"
                                    className="w-full py-6 bg-blue-600 text-white rounded-2xl font-bold uppercase tracking-wider flex items-center justify-center gap-3 shadow-lg shadow-blue-100 hover:bg-blue-700 hover:shadow-blue-200 transition-all active:scale-[0.98]"
                                >
                                    Proceed to Checkout <ArrowRight size={20}/>
                                </Link>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="py-40 text-center bg-white rounded-[3.5rem] border border-gray-100 shadow-sm flex flex-col items-center gap-8">
                        <div className="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center text-gray-200">
                            <ShoppingBag size={40} />
                        </div>
                        <div className="space-y-2">
                            <p className="text-gray-900 font-black text-xl tracking-tight">Your bag is empty</p>
                            <p className="text-gray-400 text-sm font-medium uppercase tracking-widest">Add some items to start settlement</p>
                        </div>
                        <Link href="/" className="px-12 py-5 bg-gray-900 text-white text-sm font-bold uppercase tracking-widest rounded-2xl hover:bg-blue-600 transition-all">
                            Go to Catalog
                        </Link>
                    </div>
                )}
            </div>
        </main>
    );
}