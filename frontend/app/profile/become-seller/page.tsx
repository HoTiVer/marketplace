'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { sellerService, SellerRegisterDTO } from "@/services/sellerService";
import Link from 'next/link';

export default function BecomeSellerPage() {
    const router = useRouter();
    const [formData, setFormData] = useState<SellerRegisterDTO>({
        requestedNickname: '',
        displayName: '',
        description: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await sellerService.register(formData);
            setIsSuccess(true);
        } catch (err) {
            console.error("Registration failed");
            alert("Registration failed. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isSuccess) return (
        <main className="min-h-screen bg-gray-50 flex items-center justify-center px-6 selection:bg-blue-100">
            <div className="max-w-md w-full text-center bg-white p-12 rounded-[3rem] border border-gray-100 shadow-2xl shadow-gray-200/50 animate-in fade-in zoom-in duration-300">
                <div className="w-24 h-24 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-8 shadow-inner">
                    <span className="text-5xl animate-bounce">📨</span>
                </div>
                <h1 className="text-4xl font-black tracking-tighter text-gray-900 mb-4">
                    Request <span className="text-blue-600">Sent</span>
                </h1>
                <p className="text-sm font-bold text-gray-400 uppercase tracking-widest leading-relaxed mb-10">
                    Your application is being reviewed by our administration. Check your cabinet later.
                </p>
                <Link href="/profile" className="block w-full bg-gray-900 text-white py-5 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-blue-600 shadow-xl hover:shadow-blue-200 transition-all active:scale-95">
                    Back to Cabinet
                </Link>
            </div>
        </main>
    );

    return (
        <main className="min-h-screen bg-gray-50 pt-24 pb-20 px-6 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-2xl mx-auto">

                <header className="mb-12 space-y-4">
                    <Link href="/profile" className="inline-flex items-center gap-2 text-sm font-bold text-gray-400 hover:text-blue-600 transition-colors">
                        <span className="text-lg">←</span> Cancel Registration
                    </Link>
                    <h1 className="text-6xl font-black tracking-tighter text-gray-900 leading-none">
                        Merchant <span className="text-blue-600">Apply</span>
                    </h1>
                    <p className="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400 ml-1">
                        Start your business journey today
                    </p>
                </header>

                <form onSubmit={handleSubmit} className="bg-white p-10 rounded-[3rem] border border-gray-100 shadow-xl shadow-gray-200/40">
                    <div className="space-y-8">

                        {/* Nickname Input */}
                        <div className="space-y-3">
                            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 block ml-1">Requested Nickname</label>
                            <div className="relative group">
                                <span className="absolute left-6 top-1/2 -translate-y-1/2 text-xl font-black text-blue-600/30 group-focus-within:text-blue-600 transition-colors">@</span>
                                <input
                                    required
                                    type="text"
                                    value={formData.requestedNickname}
                                    onChange={(e) => setFormData({...formData, requestedNickname: e.target.value})}
                                    placeholder="yourname"
                                    className="w-full bg-gray-50 rounded-2xl py-5 pl-12 pr-6 text-lg font-black text-gray-900 outline-none focus:bg-white focus:ring-4 ring-blue-500/10 border border-transparent focus:border-blue-500 placeholder:text-gray-300 transition-all"
                                />
                            </div>
                            <p className="text-[9px] font-bold text-gray-400 uppercase mt-2 ml-2 italic tracking-wider">This will be your unique store ID</p>
                        </div>

                        {/* Display Name Input */}
                        <div className="space-y-3">
                            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 block ml-1">Store Display Name</label>
                            <input
                                required
                                type="text"
                                value={formData.displayName}
                                onChange={(e) => setFormData({...formData, displayName: e.target.value})}
                                placeholder="THE LUXURY SHOP"
                                className="w-full bg-gray-50 rounded-2xl py-5 px-6 text-lg font-black text-gray-900 outline-none focus:bg-white focus:ring-4 ring-blue-500/10 border border-transparent focus:border-blue-500 placeholder:text-gray-300 transition-all"
                            />
                        </div>

                        {/* Description Input */}
                        <div className="space-y-3">
                            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 block ml-1">Profile Description</label>
                            <textarea
                                required
                                value={formData.description}
                                onChange={(e) => setFormData({...formData, description: e.target.value})}
                                placeholder="Describe your store and products..."
                                className="w-full bg-gray-50 rounded-2xl py-5 px-6 text-base font-semibold text-gray-700 outline-none focus:bg-white focus:ring-4 ring-blue-500/10 border border-transparent focus:border-blue-500 placeholder:text-gray-300 min-h-[160px] resize-none transition-all"
                            />
                        </div>

                    </div>

                    <div className="mt-12 space-y-6">
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full bg-gray-900 text-white py-6 rounded-2xl text-xs font-black uppercase tracking-[0.2em] hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-200 transition-all shadow-xl disabled:opacity-30 active:scale-[0.98]"
                        >
                            {isSubmitting ? 'Processing Application...' : 'Submit Application'}
                        </button>
                        <p className="text-[9px] font-bold text-center text-gray-400 uppercase tracking-[0.15em] leading-relaxed">
                            By submitting, you agree to our <span className="text-blue-600 cursor-pointer hover:underline">merchant terms of service</span>
                        </p>
                    </div>
                </form>

            </div>
        </main>
    );
}