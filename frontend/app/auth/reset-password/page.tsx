'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { authService } from '@/services/authService';
import { Loader2, Eye, EyeOff } from 'lucide-react';
import axios from "axios";

export default function ResetPasswordPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const token = searchParams.get('token');

    const [formData, setFormData] = useState({ password: '', confirm: '' });
    const [showPasswords, setShowPasswords] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleReset = async (e: React.FormEvent) => {
        if (e) e.preventDefault();

        if (formData.password !== formData.confirm) {
            setError('Passwords do not match');
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            await authService.resetPassword({
                resetToken: token || '',
                newPassword: formData.password
            });
            router.replace('/auth/login?reset=success');
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err)) {
                setError(err.response?.data?.message ?? 'Token expired or invalid');
                setIsLoading(false);
            } else {
                setError('An unexpected error occurred');
                setIsLoading(false);
            }
        }
    };

    if (!token) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-gray-50">
                <p className="text-[10px] font-black uppercase tracking-[0.4em] text-red-400">Access Denied: Missing Token</p>
            </div>
        );
    }

    return (
        <div className="flex min-h-[calc(100vh-64px)] items-center justify-center bg-gray-50 px-4">
            <div className="w-full max-w-md space-y-8 rounded-2xl bg-white p-10 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div className="text-center">
                    <h2 className="text-3xl font-black text-gray-900 tracking-tight italic uppercase">New Credentials</h2>
                    <p className="mt-2 text-sm text-gray-500 font-medium italic">Secure your account with a new password</p>
                </div>

                {error && (
                    <div className="rounded-xl bg-red-50 p-4 text-[10px] font-black uppercase tracking-widest text-red-600 border border-red-100 animate-in fade-in slide-in-from-top-1">
                        {error}
                    </div>
                )}

                <form className="mt-8 space-y-6" onSubmit={handleReset}>
                    {/* Общая кнопка управления видимостью */}
                    <div className="flex items-center justify-between px-1">
                        <span className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Security Settings</span>
                        <button
                            type="button"
                            onClick={() => setShowPasswords(!showPasswords)}
                            className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-blue-600 hover:text-blue-700 transition-colors"
                        >
                            {showPasswords ? (
                                <><EyeOff size={14} /> Hide Passwords</>
                            ) : (
                                <><Eye size={14} /> Show Passwords</>
                            )}
                        </button>
                    </div>

                    <div className="space-y-4">
                        <div>
                            <label className="text-[10px] font-black text-gray-500 uppercase tracking-[0.15em] ml-1 mb-1.5 block">
                                New Password
                            </label>
                            <input
                                type={showPasswords ? "text" : "password"}
                                required
                                value={formData.password}
                                placeholder="••••••••"
                                className="w-full rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-sm font-bold text-gray-900 transition-all focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
                                onChange={(e) => {
                                    setError('');
                                    setFormData({ ...formData, password: e.target.value });
                                }}
                            />
                        </div>

                        <div>
                            <label className="text-[10px] font-black text-gray-500 uppercase tracking-[0.15em] ml-1 mb-1.5 block">
                                Confirm Password
                            </label>
                            <input
                                type={showPasswords ? "text" : "password"}
                                required
                                value={formData.confirm}
                                placeholder="••••••••"
                                className="w-full rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-sm font-bold text-gray-900 transition-all focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-4 focus:ring-blue-500/10"
                                onChange={(e) => {
                                    setError('');
                                    setFormData({ ...formData, confirm: e.target.value });
                                }}
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full flex items-center justify-center gap-2 rounded-xl bg-blue-600 py-4 text-[11px] font-black uppercase tracking-[0.2em] text-white shadow-lg shadow-blue-200 transition-all hover:bg-blue-700 active:scale-[0.98] disabled:opacity-50"
                    >
                        {isLoading ? (
                            <>
                                <Loader2 className="animate-spin" size={16} />
                                Updating...
                            </>
                        ) : (
                            'Update Password'
                        )}
                    </button>
                </form>
            </div>
        </div>
    );
}