'use client';
import { useState, useEffect, useCallback } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { productService } from "@/services/productService";
import {
    X, ChevronLeft, Loader2, CheckCircle2, AlertCircle,
    Percent, Plus, Trash2, Calendar, Tag, ShieldCheck, Play, PowerOff, Settings, Edit2, Check, Eye
} from 'lucide-react';
import { Promotion } from "@/types/product.types";

export default function ProductPromotionsPage() {
    const router = useRouter();
    const params = useParams();
    const productId = Number(params.id);

    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [promotions, setPromotions] = useState<Promotion[]>([]);
    const [notification, setNotification] = useState<{ msg: string; type: 'error' | 'success' } | null>(null);

    const [editingId, setEditingId] = useState<number | null>(null);
    const [editForm, setEditForm] = useState({
        title: '',
        discountPercent: '',
        startTime: '',
        endTime: '',
        showEndDate: true,
        active: true
    });

    const [isAddFormOpen, setIsAddFormOpen] = useState(false);
    const [promoForm, setPromoForm] = useState({
        title: '',
        discountPercent: '',
        startTime: '',
        endTime: '',
        showEndDate: true,
        active: true
    });

    const showNotify = (msg: string, type: 'error' | 'success' = 'success') => {
        setNotification({ msg, type });
        setTimeout(() => setNotification(null), 6000);
    };

    const parseApiError = (err: unknown): string => {
        const isRecord = (value: unknown): value is Record<string, unknown> =>
            typeof value === 'object' && value !== null;

        let errorData: unknown = err;

        if (isRecord(err)) {
            if (isRecord(err.response) && err.response.data !== undefined) {
                errorData = err.response.data;
            } else if (err.data !== undefined) {
                errorData = err.data;
            }
        }

        if (isRecord(errorData)) {
            if (
                typeof errorData.message === 'string' &&
                errorData.message.trim() !== ''
            ) {
                return errorData.message;
            }

            if (isRecord(errorData.errors)) {
                const messages: string[] = [];

                Object.entries(errorData.errors).forEach(([field, val]) => {
                    if (
                        Array.isArray(val) &&
                        val.every((item): item is string => typeof item === 'string')
                    ) {
                        messages.push(`${field}: ${val.join(', ')}`);
                    } else if (typeof val === 'string') {
                        messages.push(`${field}: ${val}`);
                    }
                });

                if (messages.length > 0) {
                    return messages.join('; ');
                }
            }
        }

        return (
            productService.handleApiError(err) ||
            'Failed to process promotion campaign. Please try again.'
        );
    };

    const checkDateOverlap = (
        startVal: string,
        endVal: string,
        excludePromotionId?: number
    ): { hasOverlap: boolean; message?: string } => {
        const newStart = new Date(startVal).getTime();
        const newEnd = new Date(endVal).getTime();

        if (isNaN(newStart) || isNaN(newEnd)) {
            return { hasOverlap: true, message: "Invalid commencement or expiration date value." };
        }

        if (newStart >= newEnd) {
            return { hasOverlap: true, message: "Campaign commencement date cannot be equal to or after the expiration date." };
        }

        for (const promo of promotions) {
            if (excludePromotionId && promo.promotionId === excludePromotionId) {
                continue;
            }

            const existingStart = new Date(promo.startTime).getTime();
            const existingEnd = new Date(promo.endTime).getTime();

            const overlap = (newStart < existingEnd) && (newEnd > existingStart);

            if (overlap) {
                return {
                    hasOverlap: true,
                    message: `Campaign schedule overlaps with an existing campaign: "${promo.title}" (${new Date(promo.startTime).toLocaleDateString()} - ${new Date(promo.endTime).toLocaleDateString()})`
                };
            }
        }

        return { hasOverlap: false };
    };

    const loadPromotions = useCallback(async () => {
        if (!productId) return;
        setIsLoading(true);
        try {
            const data = await productService.getPromotions(productId);
            setPromotions(data);
        } catch (err) {
            showNotify("Could not fetch promotions dataset", 'error');
        } finally {
            setIsLoading(false);
        }
    }, [productId]);

    useEffect(() => { loadPromotions(); }, [loadPromotions]);

    const formatToDateTimeLocal = (isoString: string) => {
        if (!isoString) return '';
        const date = new Date(isoString);
        const tzOffset = date.getTimezoneOffset() * 60000;
        return (new Date(date.getTime() - tzOffset)).toISOString().slice(0, 16);
    };

    const formatToLocalISO = (dateTimeLocalValue: string) => {
        if (!dateTimeLocalValue) return '';
        const date = new Date(dateTimeLocalValue);
        const offsetMinutes = date.getTimezoneOffset();
        const absOffsetMinutes = Math.abs(offsetMinutes);
        const offsetHours = Math.floor(absOffsetMinutes / 60);
        const offsetMins = absOffsetMinutes % 60;
        const sign = offsetMinutes <= 0 ? '+' : '-';
        const pad = (n: number) => String(n).padStart(2, '0');

        const year = date.getFullYear();
        const month = pad(date.getMonth() + 1);
        const day = pad(date.getDate());
        const hours = pad(date.getHours());
        const minutes = pad(date.getMinutes());
        const seconds = '00';
        const formattedOffset = `${sign}${pad(offsetHours)}:${pad(offsetMins)}`;

        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}${formattedOffset}`;
    };

    const handleStartPreset = (preset: 'now' | 'tomorrow' | 'nextMonday', isEdit: boolean = false) => {
        const setForm = isEdit ? setEditForm : setPromoForm;
        const now = new Date();

        if (preset === 'now') {
            const tzOffset = now.getTimezoneOffset() * 60000;
            const formatted = new Date(now.getTime() - tzOffset).toISOString().slice(0, 16);
            setForm(prev => ({ ...prev, startTime: formatted }));
        } else if (preset === 'tomorrow') {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            tomorrow.setHours(9, 0, 0, 0);
            const tzOffset = tomorrow.getTimezoneOffset() * 60000;
            const formatted = new Date(tomorrow.getTime() - tzOffset).toISOString().slice(0, 16);
            setForm(prev => ({ ...prev, startTime: formatted }));
        } else if (preset === 'nextMonday') {
            const resultDate = new Date();
            resultDate.setDate(resultDate.getDate() + ((1 + 7 - resultDate.getDay()) % 7 || 7));
            resultDate.setHours(9, 0, 0, 0);
            const tzOffset = resultDate.getTimezoneOffset() * 60000;
            const formatted = new Date(resultDate.getTime() - tzOffset).toISOString().slice(0, 16);
            setForm(prev => ({ ...prev, startTime: formatted }));
        }
    };

    const handleDurationPreset = (days: number, isEdit: boolean = false) => {
        const form = isEdit ? editForm : promoForm;
        const setForm = isEdit ? setEditForm : setPromoForm;
        const baseDate = form.startTime ? new Date(form.startTime) : new Date();
        const targetDate = new Date(baseDate.getTime() + days * 24 * 60 * 60 * 1000);
        const tzOffset = targetDate.getTimezoneOffset() * 60000;
        const formatted = new Date(targetDate.getTime() - tzOffset).toISOString().slice(0, 16);
        setForm(prev => ({ ...prev, endTime: formatted }));
    };

    const handleCreatePromo = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!promoForm.title || !promoForm.discountPercent || !promoForm.startTime || !promoForm.endTime) {
            showNotify("Please fill in all parameter fields", 'error');
            return;
        }

        const validation = checkDateOverlap(promoForm.startTime, promoForm.endTime);
        if (validation.hasOverlap) {
            showNotify(validation.message || "Date configuration error", 'error');
            return;
        }

        setIsSubmitting(true);
        try {
            const payload = {
                title: promoForm.title,
                discountPercent: parseFloat(promoForm.discountPercent),
                startTime: formatToLocalISO(promoForm.startTime),
                endTime: formatToLocalISO(promoForm.endTime),
                showEndDate: promoForm.showEndDate,
                active: promoForm.active
            };

            const response:Promotion = await productService.savePromotion(productId, payload);
            const successMsg = "Promotion campaign initialized successfully!";
            showNotify(successMsg, 'success');
            setIsAddFormOpen(false);
            setPromoForm({ title: '', discountPercent: '', startTime: '', endTime: '', showEndDate: true, active: true });
            loadPromotions();
        } catch (err) {
            showNotify(parseApiError(err), 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const startEditing = (promo: Promotion) => {
        setEditingId(promo.promotionId);
        setEditForm({
            title: promo.title,
            discountPercent: String(promo.discountPercent),
            startTime: formatToDateTimeLocal(promo.startTime),
            endTime: formatToDateTimeLocal(promo.endTime),
            showEndDate: promo.showEndDate,
            active: promo.active
        });
    };

    const handleUpdatePromo = async (promotionId: number) => {
        if (!editForm.title || !editForm.discountPercent || !editForm.startTime || !editForm.endTime) {
            showNotify("Please fill in all parameter fields", 'error');
            return;
        }

        const validation = checkDateOverlap(editForm.startTime, editForm.endTime, promotionId);
        if (validation.hasOverlap) {
            showNotify(validation.message || "Date configuration error", 'error');
            return;
        }

        setIsSubmitting(true);
        try {
            const payload = {
                title: editForm.title,
                discountPercent: parseFloat(editForm.discountPercent),
                startTime: formatToLocalISO(editForm.startTime),
                endTime: formatToLocalISO(editForm.endTime),
                showEndDate: editForm.showEndDate,
                active: editForm.active
            };

            const response = await productService.updatePromotion(promotionId, payload);
            const successMsg = "Promotion changes saved successfully!";
            showNotify(successMsg, 'success');
            setEditingId(null);
            loadPromotions();
        } catch (err) {
            showNotify(parseApiError(err), 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeletePromo = async (promotionId: number) => {
        if (!confirm("Are you sure you want to deprecate this discount promotion?")) return;
        try {
            await productService.deletePromotion(promotionId);
            showNotify("Promotion deprecated successfully", 'success');
            loadPromotions();
        } catch (err) {
            showNotify("Failed to delete promotion", 'error');
        }
    };

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#f8fafc]">
                <div className="flex flex-col items-center gap-4">
                    <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
                    <span className="text-sm font-semibold tracking-wider text-slate-500 uppercase">Opening Promotions Engine...</span>
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

            <div className="max-w-[1200px] mx-auto space-y-8">

                {/* HEADER CONTROL BAR */}
                <div className="flex items-center justify-between border-b border-slate-200 pb-6">
                    <div className="space-y-2">
                        <button
                            onClick={() => router.push('/inventory')}
                            className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900 transition-colors"
                        >
                            <ChevronLeft size={16} /> Back to Catalog
                        </button>
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">
                            Discount Campaign Engine
                        </h1>
                    </div>

                    {/* ACTION BUTTONS GROUP */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => router.push(`/inventory/${productId}`)}
                            className="px-5 py-3 bg-blue-50 hover:bg-blue-100 text-blue-700 border border-blue-200 rounded-xl text-sm font-bold shadow-sm transition-all flex items-center gap-2 active:scale-95"
                        >
                            <Eye size={16} />
                            Product Page
                        </button>
                        <button
                            onClick={() => setIsAddFormOpen(!isAddFormOpen)}
                            className="px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-bold shadow-md transition-all flex items-center gap-2 active:scale-95"
                        >
                            {isAddFormOpen ? <X size={16} /> : <Plus size={16} />}
                            {isAddFormOpen ? 'Close Editor' : 'Create New Promotion'}
                        </button>
                    </div>
                </div>

                {/* NAVIGATION TABS */}
                <div className="flex border-b border-slate-200 gap-2">
                    <button
                        type="button"
                        onClick={() => router.push(`/inventory/${productId}/edit`)}
                        className="flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 border-transparent text-slate-400 hover:text-slate-600 transition-all"
                    >
                        <Settings size={16} />
                        Asset Management
                    </button>
                    <button
                        type="button"
                        className="flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 border-slate-900 text-slate-900 transition-all"
                    >
                        <Percent size={16} />
                        Discount Campaign Engine
                    </button>
                </div>

                {/* CREATION FORM PANEL */}
                {isAddFormOpen && (
                    <form onSubmit={handleCreatePromo} className="bg-white rounded-xl border border-blue-100 shadow-md p-8 space-y-6 animate-in slide-in-from-top-4 duration-300">
                        <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
                            <Tag size={20} className="text-blue-500" />
                            <h2 className="text-base font-bold text-slate-900">Define Campaign Scope</h2>
                        </div>

                        {/* Название + Процент скидки */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="space-y-2 md:col-span-2">
                                <label className="text-sm font-bold text-slate-700">Promotion Label / Title</label>
                                <input
                                    type="text"
                                    value={promoForm.title}
                                    onChange={e => setPromoForm({...promoForm, title: e.target.value})}
                                    className="w-full px-4 py-3 bg-white border border-slate-200 rounded-xl focus:border-blue-400 focus:ring-4 focus:ring-blue-100 outline-none transition-all text-sm font-medium text-slate-900"
                                    placeholder="e.g. Summer Clearance Sale"
                                />
                            </div>

                            <div className="space-y-2 md:col-span-1">
                                <label className="text-sm font-bold text-slate-700">Markdown Rate (%)</label>
                                <div className="relative">
                                    <Percent className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400" size={16}/>
                                    <input
                                        type="number"
                                        value={promoForm.discountPercent}
                                        onChange={e => setPromoForm({...promoForm, discountPercent: e.target.value})}
                                        className="w-full pl-4 pr-10 py-3 bg-white border border-slate-200 rounded-xl outline-none text-sm font-bold focus:border-blue-400 focus:ring-4 focus:ring-blue-100 transition-all text-slate-900"
                                        placeholder="15"
                                        min="1"
                                        max="100"
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Улучшенные инпуты выбора дат и времени */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                            {/* Дата начала (Commencement Date) */}
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-700 flex items-center gap-1.5">
                                    <Calendar size={15} className="text-blue-500" />
                                    Campaign Commencement Date
                                </label>
                                <div className="relative rounded-xl shadow-sm">
                                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                                        <Calendar size={18} />
                                    </div>
                                    <input
                                        type="datetime-local"
                                        value={promoForm.startTime}
                                        onChange={e => setPromoForm({...promoForm, startTime: e.target.value})}
                                        className="w-full pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-50/70 outline-none transition-all text-sm font-semibold text-slate-900"
                                    />
                                </div>
                                {/* Умные кликабельные пресеты для старта */}
                                <div className="flex flex-wrap gap-1.5 pt-1">
                                    <button
                                        type="button"
                                        onClick={() => handleStartPreset('now')}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        ⚡ Now
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleStartPreset('tomorrow')}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        🌅 Tomorrow (09:00)
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleStartPreset('nextMonday')}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        📅 Next Monday (09:00)
                                    </button>
                                </div>
                            </div>

                            {/* Дата завершения (Expiration Date) */}
                            <div className="space-y-2">
                                <label className="text-sm font-bold text-slate-700 flex items-center gap-1.5">
                                    <Calendar size={15} className="text-blue-500" />
                                    Campaign Expiration Date
                                </label>
                                <div className="relative rounded-xl shadow-sm">
                                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                                        <Calendar size={18} />
                                    </div>
                                    <input
                                        type="datetime-local"
                                        value={promoForm.endTime}
                                        onChange={e => setPromoForm({...promoForm, endTime: e.target.value})}
                                        className="w-full pl-11 pr-4 py-3 bg-white border border-slate-200 rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-50/70 outline-none transition-all text-sm font-semibold text-slate-900"
                                    />
                                </div>
                                {/* Умные кликабельные пресеты длительности */}
                                <div className="flex flex-wrap gap-1.5 pt-1">
                                    <button
                                        type="button"
                                        onClick={() => handleDurationPreset(1)}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        +1 Day
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleDurationPreset(3)}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        +3 Days
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleDurationPreset(7)}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        +1 Week
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => handleDurationPreset(30)}
                                        className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-50 hover:bg-blue-50 text-slate-600 hover:text-blue-600 font-bold transition-all border border-slate-200 hover:border-blue-200"
                                    >
                                        +1 Month
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* Опции чекбоксов */}
                        <div className="flex gap-6 pb-2">
                            <label className="flex items-center gap-2.5 cursor-pointer select-none">
                                <input
                                    type="checkbox"
                                    checked={promoForm.active}
                                    onChange={e => setPromoForm({...promoForm, active: e.target.checked})}
                                    className="w-5 h-5 text-blue-600 border-slate-200 rounded-lg focus:ring-blue-400"
                                />
                                <span className="text-sm font-bold text-slate-700">Active Immediately</span>
                            </label>

                            <label className="flex items-center gap-2.5 cursor-pointer select-none">
                                <input
                                    type="checkbox"
                                    checked={promoForm.showEndDate}
                                    onChange={e => setPromoForm({...promoForm, showEndDate: e.target.checked})}
                                    className="w-5 h-5 text-blue-600 border-slate-200 rounded-lg focus:ring-blue-400"
                                />
                                <span className="text-sm font-bold text-slate-700">Show Expiration Date</span>
                            </label>
                        </div>

                        <div className="flex justify-end gap-3 pt-2">
                            <button type="button" onClick={() => setIsAddFormOpen(false)} className="px-5 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50 rounded-lg">Cancel</button>
                            <button type="submit" disabled={isSubmitting} className="px-6 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-bold shadow-sm flex items-center gap-2">
                                {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
                                Launch Promotion
                            </button>
                        </div>
                    </form>
                )}

                {/* AUDIT LIST REGISTRY */}
                <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-8 space-y-6">
                    <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
                        <ShieldCheck size={20} className="text-slate-400" />
                        <h2 className="text-base font-bold text-slate-900">Campaign Audit Registry</h2>
                    </div>

                    {promotions.length === 0 ? (
                        <div className="text-center py-16 bg-slate-50 rounded-xl border border-dashed border-slate-200 max-w-2xl mx-auto space-y-3">
                            <Tag className="mx-auto text-slate-300" size={40} />
                            <div className="space-y-1">
                                <p className="text-sm font-bold text-slate-900">No active promotions mapped to this asset</p>
                                <p className="text-xs text-slate-500">Initiate a markdown strategy above to balance margins or boost inventory turnover.</p>
                            </div>
                        </div>
                    ) : (
                        <div className="overflow-x-auto rounded-xl border border-slate-200">
                            <table className="w-full text-left border-collapse">
                                <thead>
                                <tr className="bg-slate-50 border-b border-slate-200 text-xs font-bold text-slate-500 uppercase tracking-wider">
                                    <th className="px-6 py-4">Campaign Title</th>
                                    <th className="px-6 py-4">Markdown Rate</th>
                                    <th className="px-6 py-4">Launch Date</th>
                                    <th className="px-6 py-4">Expiration Date</th>
                                    <th className="px-6 py-4 text-center">Status</th>
                                    <th className="px-6 py-4 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-150 text-sm font-medium text-slate-800">
                                {promotions.map((promo) => {
                                    const now = new Date();
                                    const isExpired = new Date(promo.endTime) < now;
                                    const isFuture = new Date(promo.startTime) > now;
                                    const isEditing = editingId === promo.promotionId;

                                    let statusBadge = (
                                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                                            <Play size={10} fill="currentColor" /> Live Now
                                        </span>
                                    );

                                    if (!promo.active) {
                                        statusBadge = (
                                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-gray-100 text-gray-500 border border-gray-200">
                                                Inactive
                                            </span>
                                        );
                                    } else if (isExpired) {
                                        statusBadge = (
                                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-500 border border-slate-200">
                                                <PowerOff size={10} /> Expired
                                            </span>
                                        );
                                    } else if (isFuture) {
                                        statusBadge = (
                                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200">
                                                <Calendar size={10} /> Scheduled
                                            </span>
                                        );
                                    }

                                    return (
                                        <tr key={promo.promotionId} className="hover:bg-slate-50/50 transition-colors">

                                            {/* Имя кампании (Title) */}
                                            <td className="px-6 py-4 font-bold text-slate-900">
                                                {isEditing ? (
                                                    <input
                                                        type="text"
                                                        value={editForm.title}
                                                        onChange={e => setEditForm({...editForm, title: e.target.value})}
                                                        className="w-full px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-sm font-bold outline-none focus:border-blue-400"
                                                        placeholder="Campaign Title"
                                                    />
                                                ) : (
                                                    <>
                                                        {promo.title}
                                                        <span className="block text-[10px] text-slate-400 font-medium">Product: {promo.productName} (ID: #{promo.promotionId})</span>
                                                    </>
                                                )}
                                            </td>

                                            {/* Процент скидки */}
                                            <td className="px-6 py-4 font-bold">
                                                {isEditing ? (
                                                    <div className="flex items-center gap-1 max-w-[80px]">
                                                        <input
                                                            type="number"
                                                            value={editForm.discountPercent}
                                                            onChange={e => setEditForm({...editForm, discountPercent: e.target.value})}
                                                            className="w-full px-2 py-1 bg-white border border-slate-200 rounded-lg text-sm font-bold outline-none text-blue-600 focus:border-blue-400"
                                                        />
                                                        <span className="text-slate-400">%</span>
                                                    </div>
                                                ) : (
                                                    <span className="text-blue-600">-{promo.discountPercent}%</span>
                                                )}
                                            </td>

                                            {/* Дата начала */}
                                            <td className="px-6 py-4 text-xs text-slate-500 font-semibold">
                                                {isEditing ? (
                                                    <div className="space-y-1">
                                                        <div className="relative max-w-[210px]">
                                                            <div className="absolute inset-y-0 left-0 pl-2.5 flex items-center pointer-events-none text-slate-400">
                                                                <Calendar size={14} />
                                                            </div>
                                                            <input
                                                                type="datetime-local"
                                                                value={editForm.startTime}
                                                                onChange={e => setEditForm({...editForm, startTime: e.target.value})}
                                                                className="w-full pl-8 pr-2 py-1 bg-white border border-slate-200 rounded-lg text-xs font-semibold outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-50"
                                                            />
                                                        </div>
                                                        <div className="flex gap-1">
                                                            <button
                                                                type="button"
                                                                onClick={() => handleStartPreset('now', true)}
                                                                className="text-[9px] px-1.5 py-0.5 bg-slate-100 hover:bg-blue-50 hover:text-blue-600 rounded font-bold border border-slate-200 transition-all"
                                                            >
                                                                Now
                                                            </button>
                                                            <button
                                                                type="button"
                                                                onClick={() => handleStartPreset('tomorrow', true)}
                                                                className="text-[9px] px-1.5 py-0.5 bg-slate-100 hover:bg-blue-50 hover:text-blue-600 rounded font-bold border border-slate-200 transition-all"
                                                            >
                                                                Tom.
                                                            </button>
                                                        </div>
                                                    </div>
                                                ) : (
                                                    new Date(promo.startTime).toLocaleDateString('en-US', {
                                                        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                                                    })
                                                )}
                                            </td>

                                            {/* Дата окончания */}
                                            <td className="px-6 py-4 text-xs text-slate-500 font-semibold">
                                                {isEditing ? (
                                                    <div className="space-y-1">
                                                        <div className="relative max-w-[210px]">
                                                            <div className="absolute inset-y-0 left-0 pl-2.5 flex items-center pointer-events-none text-slate-400">
                                                                <Calendar size={14} />
                                                            </div>
                                                            <input
                                                                type="datetime-local"
                                                                value={editForm.endTime}
                                                                onChange={e => setEditForm({...editForm, endTime: e.target.value})}
                                                                className="w-full pl-8 pr-2 py-1 bg-white border border-slate-200 rounded-lg text-xs font-semibold outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-50"
                                                            />
                                                        </div>
                                                        <div className="flex gap-1">
                                                            <button
                                                                type="button"
                                                                onClick={() => handleDurationPreset(1, true)}
                                                                className="text-[9px] px-1.5 py-0.5 bg-slate-100 hover:bg-blue-50 hover:text-blue-600 rounded font-bold border border-slate-200 transition-all"
                                                            >
                                                                +1d
                                                            </button>
                                                            <button
                                                                type="button"
                                                                onClick={() => handleDurationPreset(7, true)}
                                                                className="text-[9px] px-1.5 py-0.5 bg-slate-100 hover:bg-blue-50 hover:text-blue-600 rounded font-bold border border-slate-200 transition-all"
                                                            >
                                                                +1w
                                                            </button>
                                                        </div>
                                                    </div>
                                                ) : (
                                                    promo.showEndDate ? (
                                                        new Date(promo.endTime).toLocaleDateString('en-US', {
                                                            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                                                        })
                                                    ) : (
                                                        <span className="text-slate-400 italic">Hidden</span>
                                                    )
                                                )}
                                            </td>

                                            {/* Статус и чекбоксы активности */}
                                            <td className="px-6 py-4 text-center">
                                                {isEditing ? (
                                                    <div className="flex flex-col gap-2 items-center text-[11px] font-bold">
                                                        <label className="flex items-center gap-1 cursor-pointer">
                                                            <input
                                                                type="checkbox"
                                                                checked={editForm.active}
                                                                onChange={e => setEditForm({...editForm, active: e.target.checked})}
                                                                className="w-4 h-4 text-blue-600 rounded"
                                                            />
                                                            Active
                                                        </label>
                                                        <label className="flex items-center gap-1 cursor-pointer">
                                                            <input
                                                                type="checkbox"
                                                                checked={editForm.showEndDate}
                                                                onChange={e => setEditForm({...editForm, showEndDate: e.target.checked})}
                                                                className="w-4 h-4 text-blue-600 rounded"
                                                            />
                                                            Show End
                                                        </label>
                                                    </div>
                                                ) : (
                                                    statusBadge
                                                )}
                                            </td>

                                            {/* Действия */}
                                            <td className="px-6 py-4 text-right">
                                                <div className="flex items-center justify-end gap-1">
                                                    {isEditing ? (
                                                        <>
                                                            <button
                                                                onClick={() => handleUpdatePromo(promo.promotionId)}
                                                                disabled={isSubmitting}
                                                                className="p-2 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-all"
                                                                title="Save parameters"
                                                            >
                                                                <Check size={16} />
                                                            </button>
                                                            <button
                                                                onClick={() => setEditingId(null)}
                                                                className="p-2 text-slate-400 hover:bg-slate-100 rounded-lg transition-all"
                                                                title="Cancel edit"
                                                            >
                                                                <X size={16} />
                                                            </button>
                                                        </>
                                                    ) : (
                                                        <>
                                                            <button
                                                                onClick={() => startEditing(promo)}
                                                                className="p-2 text-slate-400 hover:text-blue-600 hover:bg-slate-50 rounded-lg transition-all"
                                                                title="Edit campaign"
                                                            >
                                                                <Edit2 size={16} />
                                                            </button>
                                                            <button
                                                                onClick={() => handleDeletePromo(promo.promotionId)}
                                                                className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                                                                title="Revoke Campaign"
                                                            >
                                                                <Trash2 size={16} />
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}