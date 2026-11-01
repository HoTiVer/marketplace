/* eslint-disable @typescript-eslint/no-explicit-any */


'use client';
import { useState, useEffect } from 'react';
import { productService } from "@/services/productService";
import { categoryService } from "@/services/categoryService";
import { X, Plus, Package, DollarSign, Upload, Star, Trash2, Image as ImageIcon } from 'lucide-react';

export function ProductModal({ product, onClose, onRefresh, onError }: any) {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [categories, setCategories] = useState<{ id: number, name: string }[]>([]);

    const [images, setImages] = useState<any[]>([]);

    const [formData, setFormData] = useState<any>({
        name: '', price: '', description: '', categoryName: '', quantity: '', characteristics: {}
    });
    const [charRows, setCharRows] = useState<{ k: string; v: string }[]>([]);

    useEffect(() => {
        const initModal = async () => {
            try {
                const [cats, fullProduct] = await Promise.all([
                    categoryService.getAll(),
                    product?.id ? productService.getSellerProductById(product.id) : null
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
                        ...img,
                        isExisting: true
                    }));

                    setImages(serverImages.sort((a: any, b: any) => (b.isMain ? 1 : -1) - (a.isMain ? 1 : -1)));

                    if (fullProduct.characteristics) {
                        setCharRows(Object.entries(fullProduct.characteristics).map(([k, v]) => ({
                            k: String(k), v: String(v)
                        })));
                    }
                }
            } catch (err) {
                onError("Failed to initialize modal");
            }
        };
        initModal();
    }, [product]);

    const handleAddImages = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files || []);
        const newImages = files.map(file => ({
            file,
            url: URL.createObjectURL(file),
            isMain: images.length === 0,
            isExisting: false
        }));
        setImages([...images, ...newImages]);
    };

    const removeImage = (index: number) => {
        const imgToRemove = images[index];
        if (!imgToRemove.isExisting) {
            URL.revokeObjectURL(imgToRemove.url);
        }
        const newImages = images.filter((_, i) => i !== index);

        if (imgToRemove.isMain && newImages.length > 0) {
            newImages[0].isMain = true;
        }
        setImages(newImages);
    };

    const setMainImage = (index: number) => {
        const updated = images.map((img, i) => ({
            ...img,
            isMain: i === index
        }));
        setImages(updated.sort((a, b) => (b.isMain ? 1 : -1) - (a.isMain ? 1 : -1)));
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

            const mainImg = images.find(img => img.isMain);
            const fileToUpload = mainImg?.isExisting ? undefined : mainImg?.file;

            await productService.saveProduct(product?.id || null, payload, fileToUpload);
            onRefresh();
            onClose();
        } catch (err) {
            onError("Save failed");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[1000] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
            <form onSubmit={handleSubmit} className="bg-white w-full max-w-3xl rounded-3xl shadow-2xl flex flex-col max-h-[90vh] border border-slate-200">

                <div className="px-8 py-5 border-b border-slate-100 flex justify-between items-center sticky top-0 bg-white z-20">
                    <h2 className="text-xl font-black text-slate-800 tracking-tight">{product ? 'Edit' : 'Create'} Product</h2>
                    <button type="button" onClick={onClose} className="p-2 hover:bg-red-50 text-slate-400 hover:text-red-500 rounded-full transition-all"><X size={20}/></button>
                </div>

                <div className="p-8 overflow-y-auto space-y-8 scrollbar-hide">

                    {/* GALLERY SECTION */}
                    <div className="space-y-4">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] ml-1">Product Gallery</label>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                            {images.map((img, idx) => (
                                <div key={idx} className={`group relative aspect-square rounded-2xl border-2 transition-all overflow-hidden bg-slate-50 ${img.isMain ? 'border-blue-500 ring-4 ring-blue-500/10' : 'border-slate-100 hover:border-slate-200'}`}>
                                    <img src={img.url} className="w-full h-full object-cover" alt="" />

                                    {/* Badge "Main" */}
                                    {img.isMain && (
                                        <div className="absolute top-2 left-2 px-2 py-0.5 bg-blue-500 text-[8px] font-black text-white uppercase rounded-md shadow-lg">Main</div>
                                    )}

                                    {/* Overlay Actions */}
                                    <div className="absolute inset-0 bg-slate-900/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                                        {!img.isMain && (
                                            <button type="button" onClick={() => setMainImage(idx)} className="p-2 bg-white text-slate-700 rounded-full hover:text-blue-600 transition-colors shadow-xl">
                                                <Star size={14} fill={img.isMain ? "currentColor" : "none"} />
                                            </button>
                                        )}
                                        <button type="button" onClick={() => removeImage(idx)} className="p-2 bg-white text-red-500 rounded-full hover:bg-red-50 transition-colors shadow-xl">
                                            <Trash2 size={14} />
                                        </button>
                                    </div>
                                </div>
                            ))}

                            {/* Add Button */}
                            <label className="aspect-square rounded-2xl border-2 border-dashed border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 flex flex-col items-center justify-center cursor-pointer transition-all group">
                                <Upload size={24} className="text-slate-300 group-hover:text-blue-500 mb-2 transition-colors" />
                                <span className="text-[10px] font-black text-slate-400 group-hover:text-blue-600 uppercase">Add Photo</span>
                                <input type="file" multiple accept="image/*" onChange={handleAddImages} className="hidden" />
                            </label>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Name</label>
                            <input value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-100 rounded-xl focus:border-blue-500 outline-none text-sm font-bold transition-all" />
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Category</label>
                            <select value={formData.categoryName} onChange={e => setFormData({...formData, categoryName: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-100 rounded-xl focus:border-blue-500 outline-none text-sm font-bold">
                                <option value="">Select...</option>
                                {categories.map(c => <option key={c.id} value={c.name}>{c.name}</option>)}
                            </select>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Price</label>
                            <div className="relative"><DollarSign className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={16}/><input type="number" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-100 rounded-xl outline-none text-sm font-bold"/></div>
                        </div>
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Stock</label>
                            <div className="relative"><Package className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={16}/><input type="number" value={formData.quantity} onChange={e => setFormData({...formData, quantity: e.target.value})} className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-100 rounded-xl outline-none text-sm font-bold"/></div>
                        </div>
                    </div>

                    <div className="space-y-1.5">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Description</label>
                        <textarea value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} className="w-full px-4 py-4 bg-slate-50 border border-slate-100 rounded-xl min-h-[100px] text-sm font-medium resize-none"/>
                    </div>

                    <div className="space-y-4">
                        <div className="flex justify-between items-center"><label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Specs</label><button type="button" onClick={() => setCharRows([...charRows, { k: '', v: '' }])} className="text-blue-600 text-[10px] font-black uppercase tracking-wider">+ Add Spec</button></div>
                        <div className="grid grid-cols-1 gap-2">
                            {charRows.map((row, i) => (
                                <div key={i} className="flex gap-2">
                                    <input placeholder="Key" value={row.k} onChange={e => { const r = [...charRows]; r[i].k = e.target.value; setCharRows(r); }} className="flex-1 px-4 py-2 bg-white border border-slate-200 rounded-xl text-xs font-bold shadow-sm"/>
                                    <input placeholder="Value" value={row.v} onChange={e => { const r = [...charRows]; r[i].v = e.target.value; setCharRows(r); }} className="flex-1 px-4 py-2 bg-white border border-slate-200 rounded-xl text-xs font-bold shadow-sm"/>
                                    <button type="button" onClick={() => setCharRows(charRows.filter((_, idx) => idx !== i))} className="text-slate-300 hover:text-red-500 transition-colors"><X size={18}/></button>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <div className="px-8 py-6 bg-slate-50 border-t border-slate-100 flex gap-4">
                    <button type="button" onClick={onClose} className="flex-1 px-6 py-3 bg-white border border-slate-200 text-slate-700 rounded-2xl font-black text-[11px] uppercase tracking-widest active:scale-95 transition-all">Discard</button>
                    <button type="submit" disabled={isSubmitting} className="flex-[2] bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-2xl font-black text-[11px] uppercase tracking-widest shadow-lg shadow-blue-200 active:scale-95 transition-all flex items-center justify-center gap-2">
                        {isSubmitting ? 'Saving...' : 'Save Product'}
                    </button>
                </div>
            </form>
        </div>
    );
}