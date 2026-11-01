'use client';

import { useState, useEffect } from 'react';
import { userService } from "@/services/userService";
import Link from 'next/link';
import { ArrowLeft, Mail, ShieldCheck, Loader2 } from 'lucide-react';
import axios from "axios";

export default function EmailSettingsPage() {
    const [email, setEmail] = useState('user@example.com');
    const [newEmail, setNewEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [status, setStatus] = useState<{ type: 'success' | 'error', msg: string } | null>(null);

    /*
    useEffect(() => {
        const fetchEmail = async () => {
            const data = await userService.getProfile();
            setEmail(data.email);
        };
        fetchEmail();
    }, []);
    */

    const handleUpdateEmail = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setStatus(null);

        try {
            // await userService.changeEmail({ newEmail, code: verificationCode });

            setEmail(newEmail);
            setIsEditing(false);
            setNewEmail('');
            setVerificationCode('');
            setStatus({ type: 'success', msg: 'Email updated successfully!' });
        } catch (err: unknown) {
            if (axios.isAxiosError<{ message?: string }>(err))
                setStatus({ type: 'error', msg: err.response?.data?.message ?? 'Failed to update email.' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <main className="bg-gray-50 min-h-screen pt-24 p-4 md:p-8 selection:bg-blue-100">
            <div className="max-w-2xl mx-auto space-y-10">
                <header>
                    <Link href="/settings" className="text-[10px] font-black uppercase tracking-[0.3em] text-black hover:text-blue-600 transition-all flex items-center gap-2 mb-8">
                        <ArrowLeft size={14} /> Back to Settings
                    </Link>
                </header>

                <section className="bg-white p-10 md:p-14 rounded-[3rem] border border-gray-100 shadow-2xl shadow-gray-200/50 space-y-12">
                    <div className="flex items-center gap-6">
                        <div className="w-16 h-16 bg-black text-white rounded-3xl flex items-center justify-center shadow-xl">
                            <Mail size={28} />
                        </div>
                        <div>
                            <h1 className="text-3xl font-black text-black uppercase tracking-tighter italic">Email Address</h1>
                            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Communication & Login</p>
                        </div>
                    </div>

                    {!isEditing ? (
                        <div className="space-y-8 animate-in fade-in duration-500">
                            <div className="p-8 bg-gray-50 rounded-[2rem] border border-gray-100 flex justify-between items-center group">
                                <div>
                                    <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1">Current Email</p>
                                    <p className="text-lg font-black text-black">{email}</p>
                                </div>
                                <div className="w-10 h-10 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center">
                                    <ShieldCheck size={20} />
                                </div>
                            </div>

                            <button
                                onClick={() => setIsEditing(true)}
                                className="w-full py-6 bg-black text-white rounded-2xl text-[11px] font-black uppercase tracking-[0.2em] hover:bg-blue-600 transition-all active:scale-[0.98]"
                            >
                                Change Address
                            </button>
                        </div>
                    ) : (
                        <form onSubmit={handleUpdateEmail} className="space-y-6 animate-in slide-in-from-bottom-4 duration-300">
                            <div className="space-y-4">
                                <div className="relative">
                                    <label className="text-[9px] font-black text-black uppercase tracking-widest ml-4 mb-2 block">New Email Address</label>
                                    <input
                                        type="email"
                                        required
                                        value={newEmail}
                                        onChange={(e) => setNewEmail(e.target.value)}
                                        placeholder="NEW@EMAIL.COM"
                                        className="w-full bg-white border-2 border-gray-100 rounded-2xl px-6 py-4 text-sm font-black text-black placeholder:text-gray-200 focus:border-black transition-all outline-none"
                                    />
                                </div>

                                <div className="relative">
                                    <label className="text-[9px] font-black text-black uppercase tracking-widest ml-4 mb-2 block">Verification Code</label>
                                    <input
                                        type="text"
                                        required
                                        value={verificationCode}
                                        onChange={(e) => setVerificationCode(e.target.value)}
                                        placeholder="ENTER 6-DIGIT CODE"
                                        className="w-full bg-white border-2 border-gray-100 rounded-2xl px-6 py-4 text-sm font-black text-black placeholder:text-gray-200 focus:border-black transition-all outline-none text-center tracking-[0.5em]"
                                    />
                                    <p className="text-[8px] font-bold text-gray-400 uppercase mt-2 text-center">
                                        We sent a code to your current email to authorize this change.
                                    </p>
                                </div>
                            </div>

                            {status && (
                                <div className={`p-5 rounded-2xl text-[10px] font-black uppercase text-center border ${status.type === 'success' ? 'bg-emerald-50 border-emerald-100 text-emerald-600' : 'bg-red-50 border-red-100 text-red-600'}`}>
                                    {status.msg}
                                </div>
                            )}

                            <div className="flex gap-4">
                                <button
                                    type="button"
                                    onClick={() => setIsEditing(false)}
                                    className="flex-1 py-6 border-2 border-gray-100 text-black rounded-2xl text-[11px] font-black uppercase tracking-[0.2em] hover:bg-gray-50 transition-all"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={isLoading}
                                    className="flex-[2] py-6 bg-black text-white rounded-2xl text-[11px] font-black uppercase tracking-[0.2em] hover:bg-blue-600 transition-all disabled:opacity-50 flex items-center justify-center"
                                >
                                    {isLoading ? <Loader2 className="animate-spin" size={18} /> : 'Confirm Change'}
                                </button>
                            </div>
                        </form>
                    )}
                </section>

                <p className="text-center text-[9px] font-black text-gray-400 uppercase tracking-widest">
                    Your email is used for security alerts and recovery.
                </p>
            </div>
        </main>
    );
}