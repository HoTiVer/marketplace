'use client';

import { useEffect, useState } from 'react';
import { adminService } from "@/services/adminService";
import { SellerRegistrationRequest } from "@/types/seller.types";
import Link from 'next/link';

export default function SellerRequestsPage() {
    const [requests, setRequests] = useState<SellerRegistrationRequest[]>([]);
    const [loading, setLoading] = useState(true);

    const loadData = async () => {
        try {
            const data = await adminService.getSellerRequests();
            setRequests(data);
        } catch (err) {
            console.error("Failed to fetch requests");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadData(); }, []);

    const handleAction = async (id: number, type: 'approve' | 'reject') => {
        try {
            if (type === 'approve') {
                await adminService.approveSeller(id);
            } else {
                await adminService.rejectSeller(id);
            }
            setRequests(prev => prev.filter(req => req.id !== id));
        } catch (err) {
            alert("Action failed");
        }
    };

    if (loading) return (
        <div className="min-h-screen flex items-center justify-center bg-[#fafafa]">
            <div className="h-10 w-10 border-4 border-blue-600/10 border-t-blue-600 rounded-full animate-spin" />
        </div>
    );

    return (
        <main className="min-h-screen bg-[#fafafa] pt-20 pb-20 px-6 selection:bg-blue-600 selection:text-white">
            <div className="max-w-5xl mx-auto">

                <header className="mb-12 border-b-2 border-gray-900 pb-10 flex justify-between items-end">
                    <div>
                        <Link href="/admin/dashboard" className="group flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-gray-400 hover:text-blue-600 transition-colors">
                            <span className="group-hover:-translate-x-1 transition-transform">←</span> Back to Dashboard
                        </Link>
                        <h1 className="text-4xl font-black tracking-[-0.05em] text-gray-900 uppercase leading-none mt-4">
                            Seller<span className="text-blue-600">.</span>Verification
                        </h1>
                        <p className="text-[10px] font-bold uppercase tracking-[0.5em] text-gray-400 mt-3">
                            Incoming Registration Requests
                        </p>
                    </div>
                    <div className="bg-gray-900 text-white px-6 py-4 rounded-2xl shadow-xl shadow-gray-900/10 border border-gray-800">
                        <span className="text-xs font-black uppercase tracking-widest mr-3 text-gray-400">Total:</span>
                        <span className="text-xl font-black leading-none">{requests.length}</span>
                    </div>
                </header>

                <div className="grid grid-cols-1 gap-6">
                    {requests.length > 0 ? (
                        requests.map((req) => (
                            <div key={req.id} className="group relative bg-white rounded-[2.5rem] border border-blue-100 p-10 shadow-[0_20px_40px_rgba(37,99,235,0.06)] hover:shadow-[0_30px_60px_rgba(37,99,235,0.12)] hover:border-blue-400 transition-all duration-500 overflow-hidden flex flex-col lg:flex-row justify-between items-start lg:items-center gap-10">

                                <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-blue-50/50 rounded-full blur-3xl opacity-100" />

                                <div className="flex-1 relative z-10">
                                    <div className="flex items-center gap-4 mb-4">
                                        <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center text-xl border border-blue-100">
                                            👤
                                        </div>
                                        <div>
                                            <span className="text-[9px] font-black uppercase tracking-[0.2em] text-blue-600 block mb-1">New Candidate</span>
                                            <h3 className="text-2xl font-black tracking-tight text-gray-900 uppercase leading-none">@{req.requestedNickname}</h3>
                                        </div>
                                    </div>

                                    <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-6 px-1">Legal Name: {req.displayName}</p>

                                    <div className="bg-gray-50/50 border border-gray-100 p-6 rounded-[2rem] relative group-hover:bg-white group-hover:border-blue-100 transition-all duration-500">
                                        <p className="text-xs font-bold text-gray-500 uppercase tracking-wide leading-relaxed">
                                            {req.profileDescription || 'No profile description provided by the applicant.'}
                                        </p>
                                    </div>
                                </div>

                                <div className="flex flex-row lg:flex-col gap-3 w-full lg:w-48 relative z-10">
                                    <button
                                        onClick={() => handleAction(req.id, 'approve')}
                                        className="flex-1 bg-gray-900 text-white py-5 rounded-2xl text-[10px] font-black uppercase tracking-widest hover:bg-blue-600 active:scale-95 transition-all shadow-xl shadow-gray-900/10"
                                    >
                                        Approve User
                                    </button>
                                    <button
                                        onClick={() => handleAction(req.id, 'reject')}
                                        className="flex-1 bg-white text-red-500 border border-gray-100 hover:border-red-200 hover:bg-red-50 py-5 rounded-2xl text-[10px] font-black uppercase tracking-widest transition-all active:scale-95"
                                    >
                                        Decline
                                    </button>
                                </div>

                                <div className="absolute left-0 top-0 bottom-0 w-1.5 bg-blue-600 opacity-20 group-hover:opacity-100 transition-opacity duration-300" />
                            </div>
                        ))
                    ) : (
                        <div className="py-40 text-center bg-white rounded-[3rem] border-2 border-dashed border-blue-100 shadow-inner">
                            <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 text-2xl">✅</div>
                            <p className="text-[11px] font-black uppercase tracking-[0.5em] text-gray-300">No pending requests</p>
                        </div>
                    )}
                </div>
            </div>
        </main>
    );
}