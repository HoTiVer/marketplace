'use client';
import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { productService } from "@/services/productService";
import { ProductAddTypes } from "@/types/product.types";
import {
    Plus, Edit3, Trash2, Package, AlertCircle,
    CheckCircle2, ExternalLink
} from 'lucide-react';

export default function InventoryPage() {
    const router = useRouter();
    const [products, setProducts] = useState<ProductAddTypes[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<{ msg: string; type: 'error' | 'success' } | null>(null);
    const [deleteConfirm, setDeleteConfirm] = useState<{ open: boolean; id: number | null }>({ open: false, id: null });

    const showNotify = (msg: string, type: 'error' | 'success' = 'success') => {
        setNotification({ msg, type });
        setTimeout(() => setNotification(null), 3000);
    };

    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const data = await productService.getSellerProducts();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            setProducts(data as any);
        } catch (err) {
            showNotify(productService.handleApiError(err), 'error');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { loadData(); }, [loadData]);

    const executeDelete = async () => {
        if (!deleteConfirm.id) return;
        try {
            await productService.deleteProduct(deleteConfirm.id);
            showNotify("Product removed successfully");
            loadData();
        } catch (err) {
            showNotify(productService.handleApiError(err), 'error');
        } finally {
            setDeleteConfirm({ open: false, id: null });
        }
    };

    return (
        <div className="min-h-screen bg-[#f8fafc] text-slate-900 p-4 md:p-8 pt-24">
            <div className="max-w-7xl mx-auto">

                <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-10 gap-6 bg-white p-8 rounded-[2.5rem] shadow-sm border-2 border-slate-100">
                    <div>
                        <h1 className="text-3xl font-black tracking-tight text-slate-900">Inventory</h1>
                        <p className="text-slate-900 font-bold text-[10px] uppercase tracking-[0.2em] mt-1 opacity-60">Manage your digital storefront</p>
                    </div>
                    <button
                        onClick={() => router.push('/inventory/new')}
                        className="bg-slate-900 hover:bg-black text-white px-8 py-4 rounded-2xl font-black text-xs uppercase tracking-widest transition-all shadow-xl flex items-center gap-3 active:scale-95"
                    >
                        <Plus size={18} strokeWidth={3} /> Add Product
                    </button>
                </header>

                {loading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
                        {[1, 2, 3, 4].map(i => <div key={i} className="h-96 bg-white animate-pulse rounded-[2.5rem] border-2 border-slate-100" />)}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                        {products.map(prod => (
                            <div key={prod.id} className="group bg-white border-2 border-slate-100 rounded-[2.5rem] overflow-hidden hover:border-slate-900 hover:shadow-2xl transition-all duration-500 flex flex-col">

                                <div
                                    className="relative h-60 bg-slate-50 overflow-hidden cursor-pointer"
                                    onClick={() => router.push(`/product/${prod.id}`)}
                                >
                                    <img
                                        src={prod.mainImageUrl || "/images/default.png"}
                                        className="w-full h-full object-contain p-6 group-hover:scale-110 transition-transform duration-700"
                                        alt={prod.name}
                                    />
                                    <div className="absolute top-5 right-5 bg-slate-900 text-white px-4 py-2 rounded-2xl text-sm font-black shadow-lg">
                                        ${prod.price.toLocaleString()}
                                    </div>
                                    {/* Индикатор перехода (появляется при ховере) */}
                                    <div className="absolute inset-0 bg-slate-900/5 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                        <div className="bg-white/90 backdrop-blur-sm p-3 rounded-full shadow-xl translate-y-4 group-hover:translate-y-0 transition-transform">
                                            <ExternalLink size={20} className="text-slate-900" strokeWidth={3} />
                                        </div>
                                    </div>
                                </div>

                                <div className="p-7 flex-1 flex flex-col">
                                    <div className="mb-6">
                                        <div className="text-[10px] font-black text-blue-600 uppercase tracking-[0.2em] mb-2">{prod.categoryName}</div>
                                        <h3
                                            className="font-black text-slate-900 text-xl line-clamp-1 mb-2 cursor-pointer hover:text-blue-600 transition-colors"
                                            onClick={() => router.push(`/product/${prod.id}`)}
                                        >
                                            {prod.name}
                                        </h3>
                                        <div className="flex items-center gap-2">
                                            <Package size={14} className="text-slate-900" strokeWidth={2.5} />
                                            <span className={`text-[11px] font-black uppercase tracking-tight ${prod.quantity > 0 ? "text-slate-500" : "text-red-500"}`}>
                                                {prod.quantity > 0 ? `${prod.quantity} in stock` : "Out of stock"}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="flex gap-3 mt-auto pt-2">
                                        {/* Кнопка EDIT */}
                                        <button
                                            onClick={() => router.push(`/inventory/${prod.id}/edit`)}
                                            className="flex-[4] bg-white border-2 border-slate-900 text-slate-900 hover:bg-slate-900 hover:text-white py-4 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all flex items-center justify-center gap-2 active:scale-95"
                                        >
                                            <Edit3 size={14} strokeWidth={3} /> Edit
                                        </button>

                                        {/* Кнопка DELETE */}
                                        <button
                                            onClick={() => setDeleteConfirm({ open: true, id: prod.id })}
                                            className="flex-1 bg-red-50 text-red-500 hover:bg-red-500 hover:text-white py-4 rounded-xl transition-all border-2 border-transparent flex items-center justify-center active:scale-95"
                                        >
                                            <Trash2 size={18} strokeWidth={2.5} />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {deleteConfirm.open && (
                <div className="fixed inset-0 z-[1100] flex items-center justify-center p-4 bg-slate-900/80 backdrop-blur-sm animate-in fade-in duration-300">
                    <div className="bg-white w-full max-w-md rounded-[3rem] shadow-2xl border-2 border-slate-900 p-10 scale-in-center text-center">
                        <div className="w-20 h-20 bg-red-50 rounded-3xl flex items-center justify-center mb-6 mx-auto rotate-12">
                            <Trash2 className="text-red-500" size={32} strokeWidth={3} />
                        </div>
                        <h3 className="text-2xl font-black text-slate-900 mb-3 tracking-tight uppercase">Delete Product?</h3>
                        <p className="text-slate-600 font-bold mb-8 leading-relaxed">This action is permanent. <br/>Data cannot be recovered.</p>
                        <div className="flex gap-4">
                            <button
                                onClick={() => setDeleteConfirm({ open: false, id: null })}
                                className="flex-1 px-4 py-4 bg-slate-100 text-slate-900 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-slate-200 transition-all"
                            >
                                No, Cancel
                            </button>
                            <button
                                onClick={executeDelete}
                                className="flex-1 bg-red-500 hover:bg-red-600 text-white px-4 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest shadow-xl shadow-red-100 transition-all"
                            >
                                Yes, Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {notification && (
                <div className={`fixed bottom-8 right-8 z-[2000] px-8 py-5 rounded-[2rem] shadow-2xl border-2 animate-in fade-in slide-in-from-right-10 flex items-center gap-4 bg-white
                    ${notification.type === 'error' ? 'border-red-500 text-red-600' : 'border-slate-900 text-slate-900'}`}>
                    {notification.type === 'error' ? <AlertCircle size={20} strokeWidth={3}/> : <CheckCircle2 size={20} strokeWidth={3}/>}
                    <p className="text-[12px] font-black uppercase tracking-widest">{notification.msg}</p>
                </div>
            )}
        </div>
    );
}