'use client';

import { useState, useMemo } from 'react';
import type { FormEvent } from 'react';
import { userService } from '@/services/userService';
import Link from 'next/link';
import { ArrowLeft, Lock } from 'lucide-react';
import axios from 'axios';

type PasswordField = 'oldPassword' | 'newPassword' | 'confirmPassword';

interface PasswordInputProps {
    placeholder: string;
    value: string;
    name: PasswordField;
    show: boolean;
    toggle: () => void;
    onChange: (name: PasswordField, value: string) => void;
}

const PasswordInput = ({
                           placeholder,
                           value,
                           name,
                           show,
                           toggle,
                           onChange,
                       }: PasswordInputProps) => (
    <div className="relative group">
        <input
            type={show ? 'text' : 'password'}
            placeholder={placeholder}
            required
            className="w-full bg-white border-2 border-gray-100 rounded-2xl px-6 py-4 text-sm font-black text-black placeholder:text-gray-300 focus:border-black transition-all outline-none"
            onChange={(e) => onChange(name, e.target.value)}
            value={value}
        />

        <button
            type="button"
            onClick={toggle}
            className="absolute right-4 top-1/2 -translate-y-1/2 p-2 text-black hover:text-blue-600 transition-colors z-10"
        >
            <span className="text-[10px] font-black uppercase">
                {show ? 'Hide' : 'Show'}
            </span>
        </button>
    </div>
);

export default function PasswordPage() {
    const [passwords, setPasswords] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const [showOld, setShowOld] = useState(false);
    const [showNewPair, setShowNewPair] = useState(false);

    const [status, setStatus] = useState<{
        type: 'success' | 'error';
        msg: string;
    } | null>(null);

    const [isLoading, setIsLoading] = useState(false);

    const passwordStrength = useMemo(() => {
        const pass = passwords.newPassword;

        if (!pass) return 0;

        let score = 0;

        if (pass.length >= 8) score += 25;
        if (/[A-Z]/.test(pass)) score += 25;
        if (/[0-9]/.test(pass)) score += 25;
        if (/[^A-Za-z0-9]/.test(pass)) score += 25;

        return score;
    }, [passwords.newPassword]);

    const handleInputChange = (name: PasswordField, value: string) => {
        setPasswords((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handlePasswordChange = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (passwords.newPassword !== passwords.confirmPassword) {
            setStatus({
                type: 'error',
                msg: 'New passwords do not match',
            });
            return;
        }

        setIsLoading(true);

        try {
            await userService.changePassword({
                ...passwords,
                code: '',
            });

            setStatus({
                type: 'success',
                msg: 'Password changed successfully!',
            });

            setPasswords({
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
            });
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err)) {
                const message = err.response?.data?.message;

                setStatus({
                    type: 'error',
                    msg:
                        typeof message === 'string' && message.trim() !== ''
                            ? message
                            : 'Error changing password.',
                });
            } else {
                setStatus({
                    type: 'error',
                    msg: 'Error changing password.',
                });
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <main className="bg-gray-50 min-h-screen pt-24 p-4 md:p-8 selection:bg-blue-100">
            <div className="max-w-2xl mx-auto space-y-10">
                <Link
                    href="/settings"
                    className="text-[10px] font-black uppercase tracking-widest text-black hover:text-blue-600 flex items-center gap-2"
                >
                    <ArrowLeft size={14} />
                    Back to Settings
                </Link>

                <section className="bg-white p-10 md:p-14 rounded-[3rem] border border-gray-100 shadow-2xl shadow-gray-200/50 space-y-10">
                    <div className="flex items-center gap-6">
                        <div className="w-16 h-16 bg-black text-white rounded-3xl flex items-center justify-center shadow-xl">
                            <Lock size={28} />
                        </div>

                        <div>
                            <h1 className="text-3xl font-black text-black uppercase tracking-tighter italic">
                                Update Password
                            </h1>
                            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">
                                Security credentials
                            </p>
                        </div>
                    </div>

                    <form onSubmit={handlePasswordChange} className="space-y-6">
                        <PasswordInput
                            placeholder="CURRENT PASSWORD"
                            name="oldPassword"
                            value={passwords.oldPassword}
                            show={showOld}
                            toggle={() => setShowOld((prev) => !prev)}
                            onChange={handleInputChange}
                        />

                        <div className="space-y-4 pt-4 border-t border-gray-50">
                            <PasswordInput
                                placeholder="NEW PASSWORD"
                                name="newPassword"
                                value={passwords.newPassword}
                                show={showNewPair}
                                toggle={() => setShowNewPair((prev) => !prev)}
                                onChange={handleInputChange}
                            />

                            <PasswordInput
                                placeholder="CONFIRM NEW PASSWORD"
                                name="confirmPassword"
                                value={passwords.confirmPassword}
                                show={showNewPair}
                                toggle={() => setShowNewPair((prev) => !prev)}
                                onChange={handleInputChange}
                            />

                            <div className="space-y-2 pt-2">
                                <div className="flex justify-between text-[9px] font-black uppercase">
                                    <span className="text-black">Strength</span>
                                    <span className="text-blue-600">
                                        {passwordStrength}%
                                    </span>
                                </div>

                                <div className="flex gap-1.5 h-1.5">
                                    {[25, 50, 75, 100].map((lvl) => (
                                        <div
                                            key={lvl}
                                            className={`flex-1 rounded-full transition-all duration-500 ${
                                                passwordStrength >= lvl
                                                    ? 'bg-black'
                                                    : 'bg-gray-100'
                                            }`}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>

                        {status && (
                            <div
                                className={`p-5 rounded-2xl text-[10px] font-black uppercase text-center border ${
                                    status.type === 'success'
                                        ? 'bg-emerald-50 border-emerald-100 text-emerald-600'
                                        : 'bg-red-50 border-red-100 text-red-600'
                                }`}
                            >
                                {status.msg}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full bg-black text-white py-6 rounded-2xl text-[11px] font-black uppercase tracking-[0.2em] hover:bg-blue-600 transition-all active:scale-[0.98] disabled:opacity-70"
                        >
                            {isLoading ? 'Processing...' : 'Save New Password'}
                        </button>
                    </form>
                </section>
            </div>
        </main>
    );
}