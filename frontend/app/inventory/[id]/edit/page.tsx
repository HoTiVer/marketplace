/* eslint-disable @typescript-eslint/no-explicit-any */

'use client';
import { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { productService } from "@/services/productService";
import { categoryService } from "@/services/categoryService";
import {
    X, Package, DollarSign, Upload, Star,
    Trash2, ChevronLeft, Save, Loader2, CheckCircle2,
    AlertCircle, Layers, FileText, BarChart3, Maximize2,
    Settings, Percent
} from 'lucide-react';
import { PriceHistoryItem } from "@/types/product.types";

type TimePeriod = 'all' | '30d' | '6mo' | '1y';

export default function ProductEditPage() {
    const router = useRouter();
    const params = useParams();
    const productId = params.id === 'new' ? null : Number(params.id);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isHistoryLoading, setIsHistoryLoading] = useState(false);
    const [notification, setNotification] = useState<{ msg: string; type: 'error' | 'success' } | null>(null);
    const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null);
    const [isChartMaximized, setIsChartMaximized] = useState(false);
    const [timePeriod, setTimePeriod] = useState<TimePeriod>('all');

    const [categories, setCategories] = useState<{ id: number, name: string }[]>([]);
    const [images, setImages] = useState<any[]>([]);
    const [newImageFile, setNewImageFile] = useState<File | null>(null);

    const [priceHistory, setPriceHistory] = useState<PriceHistoryItem[]>([]);
    const [formData, setFormData] = useState<any>({
        name: '', price: '', description: '', categoryName: '', quantity: '', characteristics: {}
    });
    const [charRows, setCharRows] = useState<{ k: string; v: string }[]>([]);

    const showNotify = (msg: string, type: 'error' | 'success' = 'success') => {
        setNotification({ msg, type });
        setTimeout(() => setNotification(null), 6000);
    };

    const loadPriceHistory = useCallback(async () => {
        if (!productId) return;
        setIsHistoryLoading(true);
        try {
            const data = await productService.getPriceHistory(productId);
            setPriceHistory(data.sort((a, b) =>
                new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
            ));
        } catch (err) {
            console.error("Failed to load price history", err);
        } finally {
            setIsHistoryLoading(false);
        }
    }, [productId]);

    const loadData = useCallback(async () => {
        setIsLoading(true);
        try {
            const [cats, fullProduct] = await Promise.all([
                categoryService.getAll(),
                productId ? productService.getSellerProductById(productId) : null
            ]);
            setCategories(cats);
            if (fullProduct) {
                setFormData({
                    name: fullProduct.name || '',
                    price: fullProduct.price || '',
                    description: fullProduct.description || '',
                    categoryName: fullProduct.categoryName || '',
                    quantity: fullProduct.quantity || '',
                    characteristics: fullProduct.characteristics || {}
                });
                const serverImages = (fullProduct.images || []).map((img: any) => ({
                    ...img, isExisting: true
                }));
                setImages(serverImages.sort((a: any, b: any) => (b.isMain ? 1 : -1) - (a.isMain ? 1 : -1)));

                if (fullProduct.characteristics) {
                    setCharRows(Object.entries(fullProduct.characteristics).map(([k, v]) => ({
                        k: String(k), v: String(v)
                    })));
                }
                loadPriceHistory();
            }
            setNewImageFile(null);
        } catch (err) {
            showNotify("Failed to load product data", 'error');
        } finally {
            setIsLoading(false);
        }
    }, [productId, loadPriceHistory]);

    useEffect(() => { loadData(); }, [loadData]);

    const handleAddImage = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (newImageFile) {
            const oldLocalIdx = images.findIndex(img => !img.isExisting);
            if (oldLocalIdx !== -1) URL.revokeObjectURL(images[oldLocalIdx].url);
        }

        const previewUrl = URL.createObjectURL(file);
        const newImageData = {
            file,
            url: previewUrl,
            isMain: images.length === 0,
            isExisting: false
        };

        setNewImageFile(file);
        const existingImages = images.filter(img => img.isExisting);
        setImages([...existingImages, newImageData]);
    };

    const executeDelete = async (index: number) => {
        const imgToRemove = images[index];
        setDeleteConfirm(null);

        if (productId && imgToRemove.isExisting && imgToRemove.id) {
            try {
                setIsLoading(true);
                await productService.deleteProductImage(productId, imgToRemove.id);
                showNotify("Image deleted from server", 'success');
                await loadData();
            } catch (err) {
                showNotify("Could not delete image", 'error');
                setIsLoading(false);
            }
        } else {
            URL.revokeObjectURL(imgToRemove.url);
            setNewImageFile(null);
            const newImages = images.filter((_, i) => i !== index);
            if (imgToRemove.isMain && newImages.length > 0) newImages[0].isMain = true;
            setImages(newImages);
        }
    };

    const setMainImage = async (index: number) => {
        const targetImage = images[index];
        if (productId && targetImage.isExisting && targetImage.id) {
            try {
                await productService.setMainImage(productId, targetImage.id);
                await loadData();
                showNotify("Main image updated", 'success');
            } catch (err) {
                showNotify("Failed to set main image", 'error');
            }
        } else {
            const updated = images.map((img, i) => ({ ...img, isMain: i === index }));
            setImages(updated.sort((a, b) => (b.isMain ? 1 : -1) - (a.isMain ? 1 : -1)));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const finalChars: Record<string, string> = {};
            charRows.forEach(row => { if (row.k.trim()) finalChars[row.k] = row.v; });

            const payload = {
                ...formData,
                price: parseFloat(formData.price) || 0,
                quantity: parseInt(formData.quantity) || 0,
                characteristics: finalChars
            };

            await productService.saveProduct(productId, payload, newImageFile || undefined);
            showNotify(productId ? "Changes saved successfully" : "Product created", 'success');

            if (!productId) {
                setTimeout(() => router.push('/inventory'), 1500);
            } else {
                await loadData();
            }
        } catch (err) {
            showNotify(productService.handleApiError(err) || "Save operation failed", 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const filteredPriceHistory = useMemo(() => {
        if (timePeriod === 'all') return priceHistory;
        const now = new Date();
        const cutoff = new Date();
        if (timePeriod === '30d') cutoff.setDate(now.getDate() - 30);
        else if (timePeriod === '6mo') cutoff.setMonth(now.getMonth() - 6);
        else if (timePeriod === '1y') cutoff.setFullYear(now.getFullYear() - 1);
        return priceHistory.filter(item => new Date(item.createdAt) >= cutoff);
    }, [priceHistory, timePeriod]);

    const pricesOnly = useMemo(() => filteredPriceHistory.map(h => h.price), [filteredPriceHistory]);
    const maxPrice = useMemo(() => pricesOnly.length ? Math.max(...pricesOnly) : null, [pricesOnly]);
    const minPrice = useMemo(() => pricesOnly.length ? Math.min(...pricesOnly) : null, [pricesOnly]);

    const chartPoints = useMemo(() => {
        if (filteredPriceHistory.length === 0 || maxPrice === null || minPrice === null) return [];
        const chronological = [...filteredPriceHistory].reverse();
        const width = 1000;
        const height = 180;
        const paddingX = 60;
        const paddingY = 25;

        const priceRange = maxPrice - minPrice || 1;
        const pointsCount = chronological.length;

        return chronological.map((item, idx) => {
            const x = pointsCount === 1
                ? width / 2
                : paddingX + (idx / (pointsCount - 1)) * (width - paddingX * 2);
            const y = height - paddingY - ((item.price - minPrice) / priceRange) * (height - paddingY * 2);

            return {
                x, y, price: item.price,
                date: new Date(item.createdAt).toLocaleDateString('en-US', {
                    month: 'short', day: 'numeric', year: 'numeric'
                })
            };
        });
    }, [filteredPriceHistory, maxPrice, minPrice]);

    const svgPath = useMemo(() => {
        if (chartPoints.length < 2) return "";
        return chartPoints.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
    }, [chartPoints]);

    const svgAreaPath = useMemo(() => {
        if (chartPoints.length < 2) return "";
        const first = chartPoints[0];
        const last = chartPoints[chartPoints.length - 1];
        return `${svgPath} L ${last.x} 180 L ${first.x} 180 Z`;
    }, [chartPoints, svgPath]);

    const RenderSvgChart = () => {
        if (filteredPriceHistory.length === 0) {
            return (
                <div className="flex flex-col items-center justify-center h-32 text-slate-400 italic text-xs">
                    No timeline records inside this boundary.
                </div>
            );
        }
        return (
            <svg viewBox="0 0 1000 180" className="w-full h-auto overflow-visible select-none">
                <defs>
                    <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.15" />
                        <stop offset="100%" stopColor="#3b82f6" stopOpacity="0.00" />
                    </linearGradient>
                </defs>
                {chartPoints.length >= 2 && <path d={svgAreaPath} fill="url(#chartGrad)" />}
                {chartPoints.length >= 2 && <path d={svgPath} fill="none" stroke="#2563eb" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />}
                {chartPoints.map((pt, i) => (
                    <g key={i} className="group cursor-pointer">
                        <circle cx={pt.x} cy={pt.y} r="5" fill="#ffffff" stroke="#2563eb" strokeWidth="3" />
                        <text x={pt.x} y={pt.y - 12} textAnchor="middle" className="text-[20px] font-black fill-slate-800">${pt.price}</text>
                        <text x={pt.x} y={175} textAnchor="middle" className="text-[14px] font-bold fill-slate-400">{pt.date}</text>
                    </g>
                ))}
            </svg>
        );
    };

    const PeriodSelector = () => (
        <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200/60 shadow-inner">
            {(['all', '30d', '6mo', '1y'] as const).map((p) => (
                <button
                    key={p}
                    type="button"
                    onClick={(e) => {
                        e.stopPropagation();
                        setTimePeriod(p);
                    }}
                    className={`px-3 py-1 text-xs font-bold rounded-lg uppercase tracking-wider transition-all ${
                        timePeriod === p
                            ? 'bg-white text-slate-900 shadow-sm border border-slate-200/40'
                            : 'text-slate-500 hover:text-slate-900'
                    }`}
                >
                    {p === 'all' ? 'All' : p}
                </button>
            ))}
        </div>
    );

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#f8fafc]">
                <div className="flex flex-col items-center gap-4">
                    <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
                    <span className="text-sm font-semibold tracking-wider text-slate-500 uppercase">Loading Environment...</span>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#f8fafc] pt-24 pb-20 px-8 relative text-slate-900 selection:bg-slate-900 selection:text-white">

            {/* --- NOTIFICATIONS SYSTEM --- */}
            {notification && (
                <div className="fixed top-8 right-8 z-[3000] w-full max-w-md animate-in slide-in-from-top-4 fade-in duration-300">
                    <div className={`flex items-start gap-4 p-5 rounded-xl border shadow-xl bg-white ${notification.type === 'error' ? 'border-red-200 shadow-red-50' : 'border-slate-200 shadow-slate-50'}`}>
                        <div className={`p-2 rounded-lg ${notification.type === 'error' ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600'}`}>
                            {notification.type === 'error' ? <AlertCircle size={22} /> : <CheckCircle2 size={22} />}
                        </div>
                        <div className="flex-1 pt-0.5">
                            <p className="text-sm font-bold text-slate-900">{notification.msg}</p>
                        </div>
                        <button onClick={() => setNotification(null)} className="text-slate-400 hover:text-slate-600 transition-colors"><X size={16}/></button>
                    </div>
                </div>
            )}

            {/* --- DELETE CONFIRMATION MODAL --- */}
            {deleteConfirm !== null && (
                <div className="fixed inset-0 z-[1100] flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={() => setDeleteConfirm(null)} />
                    <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 p-8 relative z-10 animate-in zoom-in-95">
                        <div className="flex items-start gap-4 mb-6">
                            <div className="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center text-red-600 shrink-0">
                                <Trash2 size={24} />
                            </div>
                            <div>
                                <h3 className="text-lg font-bold text-slate-900 mb-1">Remove Asset Image</h3>
                                <p className="text-sm text-slate-500">Are you absolutely sure? This will clear the visual asset directly from the operational server database metadata.</p>
                            </div>
                        </div>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => setDeleteConfirm(null)} className="px-5 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">Cancel</button>
                            <button onClick={() => executeDelete(deleteConfirm)} className="px-5 py-2.5 text-sm font-semibold bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors shadow-sm">Confirm Erase</button>
                        </div>
                    </div>
                </div>
            )}

            {/* --- TEMPORARY MAXIMIZED CHART OVERLAY MODAL --- */}
            {isChartMaximized && priceHistory.length > 0 && (
                <div className="fixed inset-0 z-[2500] flex items-center justify-center p-6 md:p-12 animate-in fade-in duration-200">
                    <div className="absolute inset-0 bg-slate-950/60 backdrop-blur-md" onClick={() => setIsChartMaximized(false)} />
                    <div className="bg-white w-full max-w-6xl rounded-2xl shadow-2xl border border-slate-200 p-8 md:p-10 relative z-10 animate-in zoom-in-95 duration-200 flex flex-col gap-6">
                        <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-slate-100 pb-5 gap-4">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-blue-50 text-blue-600 rounded-xl">
                                    <BarChart3 size={24} />
                                </div>
                                <div>
                                    <h3 className="text-xl font-extrabold text-slate-900">Price Deviation Analytics Suite</h3>
                                    <p className="text-sm text-slate-400 font-medium">Exhaustive high-resolution analytical scale of asset historical adjustments</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-4 self-end sm:self-auto">
                                <PeriodSelector />
                                <button onClick={() => setIsChartMaximized(false)} className="p-2.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-xl transition-all">
                                    <X size={22} />
                                </button>
                            </div>
                        </div>
                        <div className="w-full bg-slate-50 rounded-xl p-6 md:p-8 border border-slate-100">
                            <RenderSvgChart />
                        </div>
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-center">
                            <div className="bg-slate-50 border border-slate-100/80 p-4 rounded-xl">
                                <span className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">Period Peak Threshold</span>
                                <span className="text-lg font-black text-emerald-600">{maxPrice ? `$${maxPrice.toLocaleString()}` : '—'}</span>
                            </div>
                            <div className="bg-slate-50 border border-slate-100/80 p-4 rounded-xl">
                                <span className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">Period Floor Baseline</span>
                                <span className="text-lg font-black text-blue-600">{minPrice ? `$${minPrice.toLocaleString()}` : '—'}</span>
                            </div>
                            <div className="bg-slate-50 border border-slate-100/80 p-4 rounded-xl">
                                <span className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">Filtered Checkpoints</span>
                                <span className="text-lg font-black text-slate-700">{filteredPriceHistory.length} entries</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* --- VERTICAL WIDE CONTENT WORKSPACE --- */}
            <div className="max-w-[1200px] mx-auto space-y-8">

                {/* GLOBAL CONTROL BAR */}
                <div className="flex items-center justify-between border-b border-slate-200 pb-6">
                    <div className="space-y-2">
                        <button onClick={() => router.push('/inventory')} className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900 transition-colors">
                            <ChevronLeft size={16} /> Back to Asset Catalog
                        </button>
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">
                            Modify Product Environment
                        </h1>
                    </div>
                    <button onClick={handleSubmit} disabled={isSubmitting} className="px-6 py-3.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-sm font-bold shadow-md transition-all flex items-center gap-2.5 active:scale-[0.98] disabled:bg-slate-300">
                        {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save size={16} />}
                        {productId ? 'Update Asset Matrix' : 'Initialize Asset Instance'}
                    </button>
                </div>

                {/* --- NAVIGATION TABS BAR (Переходы теперь роутят на отдельные страницы) --- */}
                <div className="flex border-b border-slate-200 gap-2">
                    <button
                        type="button"
                        className="flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 border-slate-900 text-slate-900 transition-all"
                    >
                        <Settings size={16} />
                        Asset Management
                    </button>
                    <button
                        type="button"
                        disabled={!productId}
                        onClick={() => router.push(`/inventory/${productId}/promotions`)}
                        className="flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 border-transparent text-slate-400 hover:text-slate-600 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                        <Percent size={16} />
                        Discount Campaign Engine
                    </button>
                </div>

                {/* --- TAB CONTENT AREA --- */}
                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start animate-in fade-in duration-200">

                    {/* LEFT AREA: FORM */}
                    <div className="lg:col-span-8 space-y-8">
                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-8 space-y-6">
                            <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
                                <FileText size={20} className="text-slate-400" />
                                <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">Core Specifications</h2>
                            </div>
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-700">Official Product Nomenclature / Title</label>
                                <input value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-4 py-3.5 text-base bg-white border border-slate-200 rounded-xl focus:border-slate-400 focus:ring-4 focus:ring-slate-100 outline-none transition-all font-medium text-slate-900" placeholder="e.g. Enterprise Microservices Compute Cluster Server Node v4" />
                            </div>
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-700">Detailed Structural Description</label>
                                <textarea value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} className="w-full px-4 py-3.5 text-base bg-white border border-slate-200 rounded-xl min-h-[220px] font-medium leading-relaxed outline-none focus:border-slate-400 focus:ring-4 focus:ring-slate-100 transition-all" placeholder="Enter exhaustive parameters, specifications overview, structural highlights, notes..." />
                            </div>
                        </div>

                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-8 space-y-6">
                            <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
                                <Package size={20} className="text-slate-400" />
                                <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">Logistics & Asset Allocation</h2>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <label className="text-sm font-bold text-slate-700">Current Unit Base Price ($)</label>
                                    <div className="relative">
                                        <DollarSign className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18}/>
                                        <input type="number" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} className="w-full pl-10 pr-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none text-base font-bold focus:border-slate-400 focus:ring-4 focus:ring-slate-100 transition-all text-slate-900" placeholder="0.00"/>
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-bold text-slate-700">Active Warehouse Stock / Quantity</label>
                                    <div className="relative">
                                        <Package className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18}/>
                                        <input type="number" value={formData.quantity} onChange={e => setFormData({...formData, quantity: e.target.value})} className="w-full pl-10 pr-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none text-base font-bold focus:border-slate-400 focus:ring-4 focus:ring-slate-100 transition-all text-slate-900" placeholder="0"/>
                                    </div>
                                </div>
                            </div>
                            <div className="space-y-2 pt-2">
                                <label className="text-sm font-bold text-slate-700">System Taxonomy Node (Category)</label>
                                <select value={formData.categoryName} onChange={e => setFormData({...formData, categoryName: e.target.value})} className="w-full px-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none text-base font-medium focus:border-slate-400 focus:ring-4 focus:ring-slate-100 transition-all cursor-pointer text-slate-900">
                                    <option value="">Select corporate organizational infrastructure layer...</option>
                                    {categories.map(c => <option key={c.id} value={c.name}>{c.name}</option>)}
                                </select>
                            </div>
                        </div>

                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-8 space-y-6">
                            <div className="flex justify-between items-center border-b border-slate-100 pb-4">
                                <div className="flex items-center gap-3">
                                    <Layers size={20} className="text-slate-400" />
                                    <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">Technical Key-Value Matrix</h2>
                                </div>
                                <button type="button" onClick={() => setCharRows([...charRows, { k: '', v: '' }])} className="text-blue-600 hover:text-blue-700 text-sm font-bold tracking-tight">+ Map New Property</button>
                            </div>
                            <div className="space-y-3">
                                {charRows.length === 0 ? (
                                    <div className="text-center py-8 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                                        <p className="text-sm text-slate-400 italic">No custom structural key parameters declared yet.</p>
                                    </div>
                                ) : (
                                    charRows.map((row, i) => (
                                        <div key={i} className="flex gap-3 items-center animate-in fade-in slide-in-from-top-2 duration-200">
                                            <input placeholder="Property Key" value={row.k} onChange={e => { const r = [...charRows]; r[i].k = e.target.value; setCharRows(r); }} className="w-1/3 px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-medium focus:border-slate-400 focus:ring-2 focus:ring-slate-100 outline-none transition-all"/>
                                            <input placeholder="Property Value" value={row.v} onChange={e => { const r = [...charRows]; r[i].v = e.target.value; setCharRows(r); }} className="flex-1 px-4 py-3 bg-white border border-slate-200 rounded-xl text-sm font-medium focus:border-slate-400 focus:ring-2 focus:ring-slate-100 outline-none transition-all"/>
                                            <button type="button" onClick={() => setCharRows(charRows.filter((_, idx) => idx !== i))} className="p-2 text-slate-400 hover:text-red-600 transition-colors rounded-lg hover:bg-slate-50"><X size={18} /></button>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>

                    {/* RIGHT AREA (С полностью завершенной и исправленной разметкой) */}
                    <div className="lg:col-span-4 space-y-8">
                        {/* Media Registry */}
                        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
                            <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 border-b border-slate-100 pb-3">Operational Media Registry</h3>
                            <div className="grid grid-cols-2 gap-3">
                                {images.map((img, idx) => (
                                    <div key={idx} className={`group relative aspect-square rounded-xl border transition-all overflow-hidden bg-slate-50 ${img.isMain ? 'border-blue-500 ring-4 ring-blue-500/10' : 'border-slate-200 hover:border-slate-400'}`}>
                                        <img src={img.url} className="w-full h-full object-cover" alt="" />
                                        {img.isMain && (
                                            <div className="absolute top-2 left-2 px-2 py-0.5 bg-blue-600 text-[10px] font-bold text-white uppercase rounded shadow-md flex items-center gap-1">
                                                <Star size={10} fill="white" /> Primary
                                            </div>
                                        )}
                                        <div className="absolute inset-0 bg-slate-950/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                                            {!img.isMain && (
                                                <button type="button" onClick={() => setMainImage(idx)} className="p-2 bg-white text-slate-900 rounded-lg hover:text-blue-600 transition-colors shadow" title="Set Core Image">
                                                    <Star size={16} />
                                                </button>
                                            )}
                                            <button type="button" onClick={() => {
                                                if (productId && images[idx].isExisting) setDeleteConfirm(idx);
                                                else executeDelete(idx);
                                            }} className="p-2 bg-white text-slate-900 rounded-lg hover:text-red-600 transition-colors shadow" title="Delete Image">
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </div>
                                ))}
                                <label className="flex flex-col items-center justify-center aspect-square border-2 border-slate-200 border-dashed rounded-xl cursor-pointer hover:bg-slate-50 transition-colors">
                                    <Upload size={20} className="text-slate-400 mb-1" />
                                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Add Asset</span>
                                    <input type="file" accept="image/*" onChange={handleAddImage} className="hidden" />
                                </label>
                            </div>
                        </div>

                        {/* Price deviation engine */}
                        {productId && (
                            <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 space-y-4">
                                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                                    <div className="flex items-center gap-2">
                                        <BarChart3 size={18} className="text-slate-400" />
                                        <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400">Price Deviation Timeline</h3>
                                    </div>
                                    {priceHistory.length > 0 && (
                                        <button type="button" onClick={() => setIsChartMaximized(true)} className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-50 rounded-lg transition-all">
                                            <Maximize2 size={16} />
                                        </button>
                                    )}
                                </div>
                                <div className="bg-slate-50 border border-slate-100 rounded-xl p-4">
                                    <RenderSvgChart />
                                </div>
                                <div className="flex justify-end pt-1">
                                    <PeriodSelector />
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}