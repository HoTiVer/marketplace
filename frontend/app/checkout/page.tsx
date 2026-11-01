'use client';

import { useState, useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { cartService } from '@/services/cartService';
import { orderService } from '@/services/orderService';
import { CartItem } from '@/types/cart.types';
import { OrderRequest } from "@/types/order.types";
import {
    ChevronLeft, Loader2, AlertCircle, CheckCircle2,
    Truck, User, Phone, MapPin, Building, ArrowRight, ShoppingBag, CreditCard, Banknote
} from 'lucide-react';
import axios from "axios";

export default function CheckoutPage() {
    const router = useRouter();

    const [items, setItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [notification, setNotification] = useState<{ msg: string; type: 'error' | 'success' } | null>(null);

    const [formData, setFormData] = useState<OrderRequest>({
        receiverName: '',
        receiverPhone: '',
        deliveryCity: '',
        deliveryAddress: '',
        paymentMethod: 'card'
    });

    const showNotify = (msg: string, type: 'error' | 'success' = 'success') => {
        setNotification({ msg, type });
        setTimeout(() => setNotification(null), 4000);
    };

    useEffect(() => {
        cartService.getCart()
            .then((data) => {
                setItems(data);
                if (data.length === 0) {
                    showNotify("Your bag is empty", "error");
                }
            })
            .catch(() => showNotify("Failed to load order preview", "error"))
            .finally(() => setLoading(false));
    }, []);

    const total = useMemo(() => items.reduce((s, i) => s + i.price * i.quantity, 0), [items]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handlePaymentSelect = (method: 'card' | 'cash') => {
        setFormData(prev => ({ ...prev, paymentMethod: method }));
    };

    const handleSubmitOrder = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.receiverName.trim() || !formData.receiverPhone.trim() || !formData.deliveryCity.trim() || !formData.deliveryAddress.trim()) {
            showNotify("Please fill in all shipping details", "error");
            return;
        }

        setIsSubmitting(true);

        try {
            const orderResponse = await orderService.createOrder(formData);

            showNotify(`Order #${orderResponse.id} placed successfully!`, "success");

            setTimeout(() => {
                router.push(`/cart`);
            }, 1500);

        } catch (error: unknown) {
            if (axios.isAxiosError<{ message?: string }>(error)) {
                const errorMsg = error?.response?.data?.message ?? "Failed to place order. Try again.";
                showNotify(errorMsg, "error");
            }
            else {
                showNotify("Failed to place order. Try again.");
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
            </div>
        );
    }

    if (items.length === 0) {
        return (
            <main className="bg-gray-50 min-h-screen p-8 flex items-center justify-center">
                <div className="text-center bg-white p-12 rounded-[2.5rem] border border-gray-100 shadow-sm max-w-md w-full space-y-6">
                    <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center text-gray-300 mx-auto">
                        <ShoppingBag size={36} />
                    </div>
                    <div className="space-y-2">
                        <h2 className="text-2xl font-black text-gray-900 tracking-tight">Your bag is empty</h2>
                        <p className="text-gray-400 text-xs font-bold uppercase tracking-wider">Add items before checking out</p>
                    </div>
                    <Link href="/" className="inline-block w-full py-4 bg-gray-900 text-white font-bold text-sm uppercase tracking-widest rounded-xl hover:bg-blue-600 transition-all text-center">
                        Return to Shop
                    </Link>
                </div>
            </main>
        );
    }

    return (
        <main className="bg-gray-50 min-h-screen p-4 md:p-8 pb-24 relative selection:bg-blue-600 selection:text-white">

            {notification && (
                <div className="fixed top-10 left-1/2 -translate-x-1/2 z-[3000] animate-in fade-in slide-in-from-top-5">
                    <div className={`flex items-center gap-3 px-6 py-4 rounded-2xl bg-white shadow-2xl border ${notification.type === 'error' ? 'border-red-100' : 'border-blue-50'}`}>
                        {notification.type === 'error' ? (
                            <AlertCircle className="text-red-500" size={18}/>
                        ) : (
                            <CheckCircle2 className="text-blue-600" size={18}/>
                        )}
                        <span className="text-sm font-bold text-gray-900 uppercase tracking-tight">{notification.msg}</span>
                    </div>
                </div>
            )}

            <div className="max-w-[1440px] mx-auto space-y-8">
                {/* HEADER */}
                <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                    <div className="space-y-2">
                        <Link href="/cart" className="flex items-center gap-2 text-xs font-bold text-gray-400 hover:text-blue-600 transition-colors uppercase tracking-widest">
                            <ChevronLeft size={16} /> Back to Bag
                        </Link>
                        <h1 className="text-5xl font-black tracking-tighter text-gray-900">Checkout</h1>
                    </div>
                </header>

                <div className="grid lg:grid-cols-12 gap-8 items-start">

                    <form onSubmit={handleSubmitOrder} className="lg:col-span-7 bg-white p-8 md:p-10 rounded-[2.5rem] border border-gray-100 shadow-sm space-y-10">

                        <div className="space-y-6">
                            <div className="flex items-center gap-3 border-b border-gray-100 pb-4">
                                <Truck className="text-blue-600" size={24} />
                                <h2 className="text-xl font-black text-gray-900 tracking-tight">Shipping Details</h2>
                            </div>

                            <div className="space-y-6">
                                {/* Receiver Name */}
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                                        <User size={14} className="text-blue-600" /> Receiver Full Name
                                    </label>
                                    <input
                                        type="text"
                                        name="receiverName"
                                        value={formData.receiverName}
                                        onChange={handleChange}
                                        placeholder="e.g. John Doe"
                                        autoComplete="name"
                                        className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl outline-none font-bold text-gray-900 focus:bg-white focus:border-blue-600 focus:ring-4 focus:ring-blue-50 transition-all text-sm"
                                        required
                                    />
                                </div>

                                {/* Receiver Phone */}
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                                        <Phone size={14} className="text-blue-600" /> Receiver Phone
                                    </label>
                                    <input
                                        type="tel"
                                        name="receiverPhone"
                                        value={formData.receiverPhone}
                                        onChange={handleChange}
                                        placeholder="+1 (555) 000-0000"
                                        autoComplete="tel"
                                        className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl outline-none font-bold text-gray-900 focus:bg-white focus:border-blue-600 focus:ring-4 focus:ring-blue-50 transition-all text-sm"
                                        required
                                    />
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {/* City */}
                                    <div className="space-y-2">
                                        <label className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                                            <Building size={14} className="text-blue-600" /> City
                                        </label>
                                        <input
                                            type="text"
                                            name="deliveryCity"
                                            value={formData.deliveryCity}
                                            onChange={handleChange}
                                            placeholder="e.g. New York"
                                            autoComplete="address-level2"
                                            className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl outline-none font-bold text-gray-900 focus:bg-white focus:border-blue-600 focus:ring-4 focus:ring-blue-50 transition-all text-sm"
                                            required
                                        />
                                    </div>

                                    {/* Delivery Address */}
                                    <div className="space-y-2">
                                        <label className="text-xs font-bold text-gray-400 uppercase tracking-wider flex items-center gap-2">
                                            <MapPin size={14} className="text-blue-600" /> Delivery Address
                                        </label>
                                        <input
                                            type="text"
                                            name="deliveryAddress"
                                            value={formData.deliveryAddress}
                                            onChange={handleChange}
                                            placeholder="e.g. 5th Avenue, Apt 12B"
                                            autoComplete="street-address"
                                            className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl outline-none font-bold text-gray-900 focus:bg-white focus:border-blue-600 focus:ring-4 focus:ring-blue-50 transition-all text-sm"
                                            required
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* PAYMENT METHOD */}
                        <div className="space-y-6">
                            <div className="flex items-center gap-3 border-b border-gray-100 pb-4">
                                <CreditCard className="text-blue-600" size={24} />
                                <h2 className="text-xl font-black text-gray-900 tracking-tight">Payment Method</h2>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <button
                                    type="button"
                                    onClick={() => handlePaymentSelect('card')}
                                    className={`p-5 rounded-2xl border flex flex-col items-center gap-3 transition-all ${
                                        formData.paymentMethod === 'card'
                                            ? 'border-blue-600 bg-blue-50/50 text-blue-600 shadow-sm'
                                            : 'border-gray-100 bg-gray-50 text-gray-400 hover:bg-white'
                                    }`}
                                >
                                    <CreditCard size={28} />
                                    <span className="text-xs font-black uppercase tracking-wider">Card</span>
                                </button>

                                <button
                                    type="button"
                                    onClick={() => handlePaymentSelect('cash')}
                                    className={`p-5 rounded-2xl border flex flex-col items-center gap-3 transition-all ${
                                        formData.paymentMethod === 'cash'
                                            ? 'border-blue-600 bg-blue-50/50 text-blue-600 shadow-sm'
                                            : 'border-gray-100 bg-gray-50 text-gray-400 hover:bg-white'
                                    }`}
                                >
                                    <Banknote size={28} />
                                    <span className="text-xs font-black uppercase tracking-wider">Cash on Delivery</span>
                                </button>
                            </div>
                        </div>

                        {/* SUBMIT BUTTON */}
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full py-6 bg-blue-600 text-white rounded-2xl font-bold uppercase tracking-wider flex items-center justify-center gap-3 shadow-lg shadow-blue-100 hover:bg-blue-700 hover:shadow-blue-200 transition-all active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="w-5 h-5 animate-spin" /> Processing Order...
                                </>
                            ) : (
                                <>
                                    Confirm & Place Order <ArrowRight size={20} />
                                </>
                            )}
                        </button>
                    </form>

                    {/* ORDER PREVIEW SIDEBAR (5 Cols) */}
                    <div className="lg:col-span-5 lg:sticky lg:top-8 space-y-6">
                        <div className="bg-white p-8 md:p-10 rounded-[2.5rem] border border-gray-100 shadow-xl shadow-gray-200/40 space-y-6">
                            <div className="flex justify-between items-center border-b border-gray-50 pb-6">
                                <h3 className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">Order Summary</h3>
                                <span className="text-xs font-black text-blue-600 uppercase tracking-widest">{items.length} items</span>
                            </div>

                            {/* ITEM LIST PREVIEW */}
                            <div className="max-h-[360px] overflow-y-auto space-y-4 pr-2">
                                {items.map((item) => (
                                    <div key={item.productId} className="flex items-center justify-between gap-4 p-3 bg-gray-50 rounded-2xl border border-gray-100">
                                        <div className="flex items-center gap-4">
                                            <div className="w-14 h-14 bg-white rounded-xl p-2 border border-gray-100 flex-shrink-0">
                                                <img src={item.mainImageUrl || '/placeholder.png'} alt={item.productName} className="w-full h-full object-contain" />
                                            </div>
                                            <div>
                                                <p className="text-sm font-black text-gray-900 line-clamp-1">{item.productName}</p>
                                                <p className="text-xs font-bold text-gray-400">Qty: {item.quantity}</p>
                                            </div>
                                        </div>
                                        <p className="text-sm font-black text-gray-900">${(item.price * item.quantity).toLocaleString()}</p>
                                    </div>
                                ))}
                            </div>

                            {/* TOTAL PRICE */}
                            <div className="border-t border-gray-50 pt-6 space-y-2">
                                <div className="flex justify-between items-baseline">
                                    <span className="text-lg font-black text-gray-900 tracking-tight">Total Amount</span>
                                    <span className="text-3xl font-black tracking-tighter text-blue-600">
                                        ${total.toLocaleString()}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </main>
    );
}