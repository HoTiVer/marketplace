'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, ShieldCheck } from 'lucide-react';

export default function TwoFAPage() {
    const [twoFA, setTwoFA] = useState(false);

    return (
        <main className="bg-gray-50 min-h-screen pt-24 p-4 md:p-8">
            <div className="max-w-2xl mx-auto space-y-10">
                <Link href="/settings" className="text-[10px] font-black uppercase tracking-widest text-black flex items-center gap-2">
                    <ArrowLeft size={14}/> Back to Settings
                </Link>

                <section className="bg-white p-10 md:p-14 rounded-[3rem] border border-gray-100 shadow-2xl shadow-gray-200/50 space-y-10">
                    <div className="flex items-center gap-6">
                        <div className="w-16 h-16 bg-blue-600 text-white rounded-3xl flex items-center justify-center shadow-xl shadow-blue-100">
                            <ShieldCheck size={28} />
                        </div>
                        <div>
                            <h1 className="text-3xl font-black text-black uppercase tracking-tighter italic">Security Layer</h1>
                            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Double protection</p>
                        </div>
                    </div>

                    <div className="bg-gray-50 p-8 rounded-[2rem] space-y-6">
                        <div className="flex justify-between items-center">
                            <span className="text-sm font-black text-black uppercase">Status: {twoFA ? 'Enabled' : 'Disabled'}</span>
                            <div className="text-3xl">{twoFA ? '🔐' : '🔓'}</div>
                        </div>

                        <p className="text-[11px] font-bold text-gray-500 leading-relaxed uppercase tracking-tight">
                            Two-factor authentication adds an extra layer of security to your account by requiring more than just a password to log in.
                        </p>

                        <button
                            onClick={() => setTwoFA(!twoFA)}
                            className={`w-full py-6 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all ${twoFA ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-black text-white'}`}
                        >
                            {twoFA ? 'Disable Protection' : 'Enable 2FA'}
                        </button>
                    </div>
                </section>
            </div>
        </main>
    );
}