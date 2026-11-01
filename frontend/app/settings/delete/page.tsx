'use client';

import Link from 'next/link';
import { ArrowLeft, Trash2, AlertTriangle } from 'lucide-react';

export default function DeleteAccountPage() {
    return (
        <main className="bg-red-50/30 min-h-screen pt-24 p-4 md:p-8">
            <div className="max-w-2xl mx-auto space-y-10">
                <Link href="/settings" className="text-[10px] font-black uppercase tracking-widest text-black flex items-center gap-2">
                    <ArrowLeft size={14}/> Back to Settings
                </Link>

                <section className="bg-white p-10 md:p-14 rounded-[3rem] border border-red-100 shadow-2xl shadow-red-100/20 space-y-10 text-center">
                    <div className="flex flex-col items-center gap-6">
                        <div className="w-20 h-20 bg-red-100 text-red-600 rounded-full flex items-center justify-center animate-pulse">
                            <AlertTriangle size={40} />
                        </div>
                        <h1 className="text-4xl font-black text-black uppercase tracking-tighter">Terminal Action</h1>
                        <p className="text-xs font-black text-red-500 uppercase tracking-[0.2em]">This cannot be undone</p>
                    </div>

                    <div className="p-8 bg-red-50 rounded-[2rem] text-left">
                        <p className="text-[11px] font-black text-red-800 uppercase leading-relaxed tracking-tight">
                            Deleting your account will erase all your personal data, project history, and active subscriptions permanently.
                        </p>
                    </div>

                    <div className="space-y-4">
                        <input
                            type="text"
                            placeholder="TYPE 'DELETE' TO CONFIRM"
                            className="w-full bg-white border-2 border-red-100 rounded-2xl px-6 py-4 text-center text-sm font-black text-red-600 placeholder:text-red-200 focus:border-red-500 outline-none transition-all"
                        />
                        <button className="w-full bg-red-600 text-white py-6 rounded-2xl text-[11px] font-black uppercase tracking-[0.2em] hover:bg-red-700 shadow-xl shadow-red-200 transition-all">
                            Permanent Termination
                        </button>
                    </div>
                </section>
            </div>
        </main>
    );
}