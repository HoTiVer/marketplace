'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, Loader2, Mail } from 'lucide-react';
import { authService } from '@/services/authService';
import axios from "axios";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isSent, setIsSent] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            await authService.requestPasswordReset({ email });
            setIsSent(true);
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err)) {
                setError(err.response?.data?.message ?? 'Email not found');
            } else {
                setError('An unexpected error occurred');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex min-h-[calc(100vh-64px)] items-center justify-center bg-gray-50 px-4">
            <div className="w-full max-w-md space-y-8 rounded-2xl bg-white p-10 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div className="text-center">
                    <h2 className="text-3xl font-black text-gray-900 tracking-tight italic uppercase">Recover</h2>
                    <p className="mt-2 text-sm text-gray-500 font-medium italic">Enter email to receive reset link</p>
                </div>

                {error && (
                    <div className="rounded-xl bg-red-50 p-4 text-xs font-bold uppercase tracking-wide text-red-600 border border-red-100">
                        {error}
                    </div>
                )}

                {!isSent ? (
                    <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                        <div>
                            <label className="text-[10px] font-black text-gray-500 uppercase tracking-[0.15em] ml-1 mb-1.5 block">
                                Email Address
                            </label>
                            <input
                                type="email"
                                required
                                placeholder="name@company.com"
                                className="w-full rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-sm font-bold text-gray-900 transition-all focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full flex items-center justify-center gap-2 rounded-xl bg-blue-600 py-4 text-[11px] font-black uppercase tracking-[0.1em] text-white shadow-lg shadow-blue-200 transition-all hover:bg-blue-700 disabled:opacity-70 active:scale-[0.98]"
                        >
                            {isLoading ? <Loader2 className="animate-spin" size={16} /> : 'Send Reset Link'}
                        </button>
                    </form>
                ) : (
                    <div className="text-center py-4 space-y-4 animate-in fade-in zoom-in-95">
                        <div className="mx-auto w-12 h-12 bg-green-50 text-green-600 rounded-full flex items-center justify-center">
                            <Mail size={24} />
                        </div>
                        <p className="text-xs font-bold text-gray-600 uppercase tracking-widest leading-relaxed">
                            Check your inbox.<br/>Link sent successfully.
                        </p>
                    </div>
                )}

                <div className="pt-4 text-center">
                    <Link href="/auth/login" className="inline-flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-gray-400 hover:text-blue-600 transition-colors">
                        <ArrowLeft size={12} /> Back to Login
                    </Link>
                </div>
            </div>
        </div>
    );
}