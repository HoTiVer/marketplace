'use client';

import { useEffect, useState } from 'react';
import { useAuth } from "@/context/AuthContext";
import { categoryService } from "@/services/categoryService";
import { CategoryType } from "@/types/category.types";

export default function CategoriesPage() {
    const { hasRole, isLoading: authLoading } = useAuth();
    const [categories, setCategories] = useState<CategoryType[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingCategory, setEditingCategory] = useState<CategoryType | null>(null);
    const [categoryName, setCategoryName] = useState('');
    const [categoryToDelete, setCategoryToDelete] = useState<CategoryType | null>(null);

    const isAdmin = hasRole('ADMIN');

    const loadCategories:()=> Promise<void> = async () => {
        try {
            const data:CategoryType[] = await categoryService.getAll();
            setCategories(data);
        } catch (err) {
            setError('Failed to load categories');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { loadCategories(); }, []);

    const handleSave:()=> Promise<void> = async (): Promise<void> => {
        if (!categoryName.trim()) return;
        try {
            if (editingCategory) await categoryService.update(editingCategory.id, categoryName);
            else await categoryService.create(categoryName);
            setIsModalOpen(false);
            setCategoryName('');
            loadCategories();
        } catch (err) { alert('Error saving'); }
    };

    const confirmDelete = async () => {
        if (!categoryToDelete) return;
        try {
            await categoryService.delete(categoryToDelete.id);
            setCategoryToDelete(null);
            loadCategories();
        } catch (err) { alert('Error deleting'); }
    };

    if (authLoading || isLoading) return (
        <div className="flex min-h-[80vh] items-center justify-center bg-gray-50">
            <div className="h-10 w-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    return (
        <main className="min-h-screen bg-gray-50 pt-24 pb-20 px-4 md:px-8 selection:bg-blue-100">
            <div className="max-w-5xl mx-auto">

                {/* Компактный Header */}
                <header className="flex items-center justify-between mb-8 border-b border-gray-200 pb-8">
                    <div>
                        <h1 className="text-3xl font-black tracking-tighter text-gray-900 uppercase italic">
                            Categories <span className="text-blue-600">Editor</span>
                        </h1>
                        <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-1">
                            {categories.length} segments defined
                        </p>
                    </div>

                    {isAdmin && (
                        <button
                            onClick={() => { setEditingCategory(null); setCategoryName(''); setIsModalOpen(true); }}
                            className="bg-gray-900 text-white px-6 py-3 rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-blue-600 transition-all active:scale-95 flex items-center gap-2"
                        >
                            <span>+</span> Create New
                        </button>
                    )}
                </header>

                {error && <div className="bg-red-50 text-red-600 p-4 rounded-xl text-xs font-bold mb-6">{error}</div>}

                {/* Компактный список */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {categories.length === 0 ? (
                        <div className="col-span-full py-20 text-center bg-white rounded-3xl border border-gray-100 text-gray-400 text-xs font-bold uppercase tracking-widest">
                            No data available
                        </div>
                    ) : (
                        categories.map((cat) => (
                            <div
                                key={cat.id}
                                className="group bg-white p-4 pr-6 rounded-2xl border border-gray-100 hover:border-blue-200 hover:shadow-lg hover:shadow-blue-50 transition-all flex items-center justify-between"
                            >
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-lg grayscale group-hover:grayscale-0 transition-all">
                                        📂
                                    </div>
                                    <div>
                                        <h2 className="text-sm font-black text-gray-900 truncate max-w-[150px] md:max-w-[200px]">
                                            {cat.name}
                                        </h2>
                                        <span className="text-[9px] font-bold text-gray-300 uppercase tracking-tighter">REF-ID: {cat.id}</span>
                                    </div>
                                </div>

                                {isAdmin && (
                                    <div className="flex items-center gap-4">
                                        <button
                                            onClick={() => { setEditingCategory(cat); setCategoryName(cat.name); setIsModalOpen(true); }}
                                            className="text-[10px] font-black text-gray-400 hover:text-blue-600 uppercase tracking-widest transition-colors"
                                        >
                                            Edit
                                        </button>
                                        <button
                                            onClick={() => setCategoryToDelete(cat)}
                                            className="text-[10px] font-black text-gray-400 hover:text-red-500 uppercase tracking-widest transition-colors"
                                        >
                                            Del
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>
            </div>

            {/* Компактная модалка - Create/Edit */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-gray-900/40 backdrop-blur-sm flex items-center justify-center z-[100] p-4">
                    <div className="bg-white rounded-[2.5rem] p-10 w-full max-w-sm shadow-2xl border border-gray-100 animate-in zoom-in duration-200">
                        <div className="mb-8">
                            <h2 className="text-2xl font-black tracking-tighter text-gray-900 uppercase italic">
                                {editingCategory ? 'Update' : 'New Entry'}
                            </h2>
                        </div>

                        <div className="space-y-4 mb-8">
                            <input
                                type="text"
                                autoFocus
                                value={categoryName}
                                onChange={(e) => setCategoryName(e.target.value)}
                                placeholder="Category name..."
                                className="w-full bg-gray-50 border border-transparent rounded-xl py-4 px-5 text-sm font-bold text-gray-900 outline-none focus:bg-white focus:ring-4 ring-blue-500/10 focus:border-blue-500 transition-all"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="py-4 text-[10px] font-black text-gray-400 uppercase tracking-widest hover:text-gray-900"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSave}
                                className="bg-blue-600 text-white py-4 rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-blue-700 shadow-lg shadow-blue-100 transition-all active:scale-95"
                            >
                                Save Entry
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Маленькая модалка подтверждения удаления */}
            {categoryToDelete && (
                <div className="fixed inset-0 bg-gray-900/40 backdrop-blur-sm flex items-center justify-center z-[100] p-4">
                    <div className="bg-white rounded-[2.5rem] p-8 w-full max-w-xs shadow-2xl border border-gray-100 text-center animate-in fade-in zoom-in duration-200">
                        <p className="text-xs font-black text-gray-400 uppercase tracking-widest mb-2">Confirm Delete?</p>
                        <h3 className="text-lg font-black text-gray-900 mb-8 italic">{categoryToDelete.name}</h3>

                        <div className="flex flex-col gap-2">
                            <button
                                onClick={confirmDelete}
                                className="w-full py-4 bg-red-500 text-white rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-red-600 transition-all active:scale-95"
                            >
                                Delete Forever
                            </button>
                            <button
                                onClick={() => setCategoryToDelete(null)}
                                className="w-full py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest"
                            >
                                Back
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </main>
    );
}