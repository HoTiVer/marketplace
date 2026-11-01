'use client';

import { useEffect, useState, useCallback, use } from 'react';
import Link from 'next/link';
import { sellerService } from "@/services/sellerService";
import { ProductSummary } from "@/types/product.types";
import ProductCard from "@/components/ProductCard";

export default function SellerProductsPage({ params }: { params: Promise<{ nickname: string }> }) {
    const { nickname } = use(params);

    const [products, setProducts] = useState<ProductSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const loadProducts = useCallback(async () => {
        try {
            setIsLoading(true);
            const data = await sellerService.getProductsByNickname(nickname);
            setProducts(data);
        } catch (err: unknown) {
            setError('The collection is temporarily unavailable.');
        } finally {
            setIsLoading(false);
        }
    }, [nickname]);

    useEffect(() => {
        loadProducts();
    }, [loadProducts]);

    if (isLoading) return (
        <div className="flex min-h-screen items-center justify-center bg-[#fafafa]">
            <div className="h-10 w-10 border-4 border-blue-600/10 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    return (
        <div className="min-h-screen bg-[#fafafa] selection:bg-blue-600 selection:text-white">
            <main className="max-w-[1440px] mx-auto pt-24 p-6 pb-32">

                {/* Header Section — Строгий Brutalist */}
                <header className="relative mb-20 p-10 md:p-16 bg-white rounded-[3.5rem] border border-blue-100 shadow-[0_30px_70px_rgba(37,99,235,0.04)] overflow-hidden flex flex-col md:flex-row md:items-end justify-between gap-12">
                    {/* Фоновое свечение */}
                    <div className="absolute -right-20 -top-20 w-80 h-80 bg-blue-50/40 rounded-full blur-[100px] pointer-events-none" />

                    <div className="relative z-10 max-w-3xl">
                        <Link
                            href={`/seller/${nickname}`}
                            className="group flex items-center gap-2 text-[10px] font-black uppercase tracking-[0.4em] text-gray-400 hover:text-blue-600 transition-all mb-8 block"
                        >
                            <span className="group-hover:-translate-x-1 transition-transform">←</span> Return to Profile
                        </Link>

                        <div className="space-y-4">
                            <div className="flex items-center gap-3">
                                <span className="w-10 h-[2px] bg-blue-600"></span>
                                <span className="text-[10px] font-black uppercase tracking-[0.5em] text-blue-600/50">Merchant Inventory</span>
                            </div>
                            <h1 className="text-6xl md:text-8xl font-black tracking-[-0.05em] text-gray-900 uppercase leading-none">
                                Collection<span className="text-blue-600">.</span>
                            </h1>
                            <p className="text-[11px] font-bold text-gray-400 uppercase tracking-[0.4em] mt-6 bg-gray-50 inline-block px-4 py-2 rounded-lg border border-gray-100">
                                Curated Archive / @{nickname}
                            </p>
                        </div>
                    </div>

                    <div className="relative z-10 flex flex-col items-end">
                        <div className="bg-gray-900 text-white px-8 py-6 rounded-2xl shadow-2xl shadow-gray-900/10 border border-gray-800">
                            <span className="text-[10px] font-black uppercase tracking-[0.3em] block mb-2 text-gray-400">Total Units</span>
                            <span className="text-4xl font-black leading-none">{products.length}</span>
                        </div>
                    </div>

                    {/* Акцентная линия слева */}
                    <div className="absolute left-0 top-0 bottom-0 w-2 bg-blue-600 opacity-20" />
                </header>

                {/* Content Area */}
                {error ? (
                    <div className="bg-white p-24 rounded-[3.5rem] border border-blue-100 text-center shadow-xl">
                        <p className="text-[11px] font-black uppercase tracking-[0.4em] text-gray-300 mb-10">{error}</p>
                        <button
                            onClick={loadProducts}
                            className="px-10 py-5 bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest rounded-2xl hover:bg-blue-600 transition-all shadow-lg"
                        >
                            Retry Connection
                        </button>
                    </div>
                ) : products.length === 0 ? (
                    <div className="bg-white py-40 rounded-[3.5rem] border border-dashed border-blue-100 text-center">
                        <div className="text-5xl mb-8 opacity-10">NULL_ARCHIVE</div>
                        <p className="text-[11px] font-black uppercase tracking-[0.5em] text-gray-300">
                            The requested collection is currently empty.
                        </p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-8 gap-y-16">
                        {products.map((product) => (
                            <div key={product.productId} className="group relative">
                                {/* Стационарное свечение под каждой карточкой */}
                                <div className="absolute inset-0 bg-blue-50/30 rounded-[2.5rem] blur-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

                                <div className="relative z-10 transform group-hover:-translate-y-2 transition-all duration-500">
                                    <ProductCard product={product} />

                                    {/* Динамическая бруталистская подпись под карточкой */}
                                    <div className="mt-4 px-4 flex justify-between items-center opacity-40 group-hover:opacity-100 transition-opacity">
                                        <span className="text-[8px] font-black uppercase tracking-widest text-gray-400">
                                            Unit ID: {product.productId.toString().padStart(4, '0')}
                                        </span>
                                        {product.promotionValue ? (
                                            <span className="text-[8px] font-black uppercase tracking-widest text-red-600 animate-pulse">
                                                Promo Active
                                            </span>
                                        ) : (
                                            <span className="text-[8px] font-black uppercase tracking-widest text-blue-600">
                                                Stock Ready
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}