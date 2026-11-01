'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/authService';
import { useAuth } from "@/context/AuthContext";
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import axios from "axios";

export default function LoginPage() {
    const { checkAuth } = useAuth();
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const router = useRouter();

    const handleLogin = async (e: React.FormEvent) => {
        if (e) e.preventDefault();
        if (isLoading) return;

        setIsLoading(true);
        setError('');

        try {
            await authService.login(formData);
            await checkAuth();
            router.replace('/');
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err)) {
                const status = err.response?.status;
                const message = err.response?.data?.message;

                if (status === 401) {
                    setError('Invalid email or password');
                } else if (status === 422) {
                    setError('Validation failed. Please check your data.');
                } else {
                    setError(message ?? 'Something went wrong');
                }
            } else {
                setError('Something went wrong');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleGoogleLogin = (e: React.MouseEvent) => {
        e.preventDefault();
        window.location.href = authService.getOAuth2Url('google');
    };

    return (
        <div className="flex min-h-[calc(100vh-64px)] items-center justify-center bg-gray-50 px-4">
            <div className="w-full max-w-md space-y-8 rounded-2xl bg-white p-10 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div className="text-center">
                    <h2 className="text-3xl font-black text-gray-900 tracking-tight italic uppercase">Welcome Back</h2>
                    <p className="mt-2 text-sm text-gray-500 font-medium italic">Please enter your details to sign in</p>
                </div>

                {error && (
                    <div className="rounded-xl bg-red-50 p-4 text-[10px] font-black uppercase tracking-widest text-red-600 border border-red-100 animate-in fade-in slide-in-from-top-1">
                        {error}
                    </div>
                )}

                <form className="mt-8 space-y-5" onSubmit={handleLogin}>
                    <div className="space-y-4">
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-gray-500 uppercase tracking-[0.15em] ml-1">Email Address</label>
                            <input
                                type="email"
                                required
                                value={formData.email}
                                className="w-full rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-sm font-bold text-gray-900 focus:bg-white focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            />
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-gray-500 uppercase tracking-[0.15em] ml-1">Password</label>
                            <div className="relative">
                                <input
                                    type={showPassword ? "text" : "password"}
                                    required
                                    value={formData.password}
                                    className="w-full rounded-xl border border-gray-100 bg-gray-50 px-4 py-3.5 text-sm font-bold text-gray-900 focus:bg-white focus:ring-4 focus:ring-blue-500/10 outline-none transition-all"
                                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 p-2 text-gray-400 hover:text-blue-600 transition-colors"
                                >
                                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="flex justify-end">
                        <Link href="/auth/forgot-password" className="text-[10px] font-black uppercase tracking-widest text-gray-400 hover:text-black transition-colors">
                            Forgot Password?
                        </Link>
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full flex items-center justify-center gap-2 rounded-xl bg-blue-600 py-4 text-[11px] font-black uppercase tracking-[0.2em] text-white shadow-lg shadow-blue-200 hover:bg-blue-700 active:scale-[0.98] disabled:opacity-50 transition-all"
                    >
                        {isLoading ? <Loader2 className="animate-spin" size={16} /> : 'Sign In'}
                    </button>
                </form>

                <div className="relative flex items-center justify-center my-8">
                    <div className="w-full border-t border-gray-100"></div>
                    <div className="absolute bg-white px-4 text-[10px] font-black text-gray-300 uppercase tracking-[0.2em]">
                        Social Auth
                    </div>
                </div>

                <button
                    type="button"
                    onClick={handleGoogleLogin}
                    className="w-full flex items-center justify-center gap-3 rounded-xl border border-gray-100 bg-white py-3.5 text-xs font-black uppercase tracking-wider text-gray-700 transition-all hover:bg-gray-50 hover:border-gray-200 active:scale-[0.98] shadow-sm"
                >
                    <svg className="w-4 h-4" viewBox="0 0 48 48">
                        <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                        <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                        <path fill="#FBBC05" d="M10.54 28.55c-.48-1.45-.76-2.99-.76-4.55s.28-3.1.76-4.55l-7.98-6.19C.92 16.46 0 20.12 0 24s.92 7.54 2.56 10.78l7.98-6.23z"/>
                        <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.46-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                    </svg>
                    Continue with Google
                </button>

                <p className="text-center text-[11px] font-bold text-gray-400 uppercase tracking-tight">
                    Don&apos;t have an account?{' '}
                    <Link href="/auth/register" className="text-blue-600 hover:text-blue-700 underline-offset-4 hover:underline transition-all">
                        Create Account
                    </Link>
                </p>
            </div>
        </div>
    );
}