'use client';

import { useEffect, useState } from 'react';
import { userService } from "@/services/userService";
import { PersonalInfo } from "@/types/user.types";
import Link from 'next/link';

export default function CabinetPage() {
    const [user, setUser] = useState<PersonalInfo | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadUser = async () => {
            try {
                const data = await userService.getPersonalInfo();
                setUser(data);
            } catch (err) {
                console.error("Failed to load profile");
            } finally {
                setLoading(false);
            }
        };
        loadUser();
    }, []);

    if (loading) return (
        <div className="flex min-h-[80vh] items-center justify-center bg-gray-50">
            <div className="h-10 w-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    return (
        <main className="bg-gray-50 min-h-screen pt-24 p-4 md:p-8 pb-24 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-6xl mx-auto space-y-8">

                <header className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                    <div>
                        <h1 className="text-5xl font-black tracking-tighter text-gray-900 leading-none">
                            Personal <span className="text-blue-600">Cabinet</span>
                        </h1>
                        <p className="text-gray-400 font-bold uppercase tracking-widest text-[10px] mt-4 ml-1">
                            Account Management & Overview
                        </p>
                    </div>
                    <div className="flex gap-3">
                        <Link href="/profile/settings">
                            <button className="px-6 py-3 bg-white border border-gray-200 text-gray-700 text-xs font-black uppercase tracking-widest rounded-xl hover:bg-gray-50 transition-all shadow-sm active:scale-95">
                                Settings
                            </button>
                        </Link>
                        <button className="px-6 py-3 bg-blue-600 text-white text-xs font-black uppercase tracking-widest rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-200 transition-all active:scale-95">
                            Edit Profile
                        </button>
                    </div>
                </header>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

                    {/* Left Column: User Info & Stats */}
                    <div className="lg:col-span-7 space-y-8">
                        <section className="bg-white p-10 rounded-[2.5rem] border border-gray-100 shadow-xl shadow-gray-200/50">
                            <div className="flex justify-between items-center mb-10 pb-6 border-b border-gray-50">
                                <span className="px-4 py-1.5 bg-blue-50 text-blue-600 text-[10px] font-black uppercase tracking-widest rounded-full border border-blue-100">
                                    Identity Verified
                                </span>
                                <div className="text-right">
                                    <p className="text-[9px] font-black uppercase text-gray-300 tracking-[0.2em]">Member Since</p>
                                    <p className="text-xs font-black text-gray-900">{user ? new Date(user.registerDate).toLocaleDateString() : '—'}</p>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest block ml-1">Display Name</label>
                                    <p className="text-3xl font-black tracking-tighter text-gray-900 truncate">
                                        {user?.displayName}
                                    </p>
                                </div>

                                <div className="space-y-1">
                                    <label className="text-[10px] font-black uppercase text-gray-400 tracking-widest block ml-1">Email Address</label>
                                    <p className="text-xl font-bold text-gray-600 truncate italic">
                                        {user?.email}
                                    </p>
                                </div>
                            </div>
                        </section>

                        {/* Fast Navigation */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <Link href="/cabinet/orders" className="bg-white p-8 rounded-3xl border border-gray-100 hover:border-blue-200 transition-all group shadow-sm hover:shadow-xl hover:shadow-blue-50">
                                <div className="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center text-3xl mb-6 shadow-inner group-hover:scale-110 transition-transform duration-300">
                                    📦
                                </div>
                                <p className="text-xs font-black uppercase tracking-widest text-gray-900">My Orders</p>
                                <p className="text-[10px] font-bold text-gray-400 uppercase mt-1">Track & History</p>
                            </Link>

                            <Link href="/wishlist" className="bg-white p-8 rounded-3xl border border-gray-100 hover:border-red-200 transition-all group shadow-sm hover:shadow-xl hover:shadow-red-50">
                                <div className="w-14 h-14 bg-red-50 rounded-2xl flex items-center justify-center text-3xl mb-6 shadow-inner group-hover:scale-110 transition-transform duration-300">
                                    ❤️
                                </div>
                                <p className="text-xs font-black uppercase tracking-widest text-gray-900">Wishlist</p>
                                <p className="text-[10px] font-bold text-gray-400 uppercase mt-1">Saved items</p>
                            </Link>
                        </div>
                    </div>

                    {/* Right Column: Seller Status */}
                    <div className="lg:col-span-5">
                        {user?.isSeller ? (
                            <div className="bg-gray-900 text-white p-10 rounded-[3rem] shadow-2xl shadow-gray-900/40 sticky top-24 overflow-hidden relative group">
                                {/* Декоративный элемент фона */}
                                <div className="absolute -right-10 -top-10 w-40 h-40 bg-blue-600/20 rounded-full blur-3xl group-hover:bg-blue-600/30 transition-colors" />

                                <div className="relative z-10">
                                    <div className="flex justify-between items-center mb-12">
                                        <div className="flex items-center gap-2">
                                            <span className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse" />
                                            <span className="text-[10px] font-black uppercase tracking-[0.3em] text-white/50">Merchant Account</span>
                                        </div>
                                        <span className="text-2xl">💎</span>
                                    </div>

                                    <div className="space-y-1 mb-10">
                                        <p className="text-[10px] font-black uppercase text-white/30 tracking-[0.2em] ml-1">Store Identity</p>
                                        <h4 className="text-4xl font-black tracking-tighter italic uppercase leading-none">
                                            @{user.sellerNickname}
                                        </h4>
                                    </div>

                                    <Link
                                        href="/seller/dashboard"
                                        className="block w-full text-center bg-blue-600 text-white py-5 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-blue-500 hover:shadow-lg hover:shadow-blue-600/40 transition-all active:scale-95 shadow-xl"
                                    >
                                        Merchant Studio →
                                    </Link>

                                    <div className="mt-8 pt-6 border-t border-white/10 flex items-center justify-center gap-2">
                                        <span className="text-[10px] font-bold text-white/20 uppercase tracking-widest italic">
                                            Trusted Partner since 2026
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="bg-white p-10 rounded-[3rem] border-2 border-dashed border-gray-100 flex flex-col items-center text-center justify-center min-h-[400px] shadow-sm relative overflow-hidden">
                                <div className="w-20 h-20 bg-gray-50 rounded-3xl flex items-center justify-center shadow-inner mb-6 group-hover:rotate-12 transition-transform">
                                    <span className="text-4xl">🏪</span>
                                </div>
                                <h4 className="text-2xl font-black text-gray-900 tracking-tighter mb-3 uppercase italic">Merchant Hub</h4>
                                <p className="text-xs font-bold text-gray-400 uppercase leading-relaxed mb-10 max-w-[220px]">
                                    Open your own store and start earning today.
                                </p>
                                <Link
                                    href="/profile/become-seller"
                                    className="w-full bg-gray-900 text-white text-xs font-black uppercase tracking-widest py-5 rounded-2xl hover:bg-blue-600 transition-all shadow-xl active:scale-95"
                                >
                                    Apply for Seller
                                </Link>
                            </div>
                        )}
                    </div>
                </div>

            </div>
        </main>
    );
}