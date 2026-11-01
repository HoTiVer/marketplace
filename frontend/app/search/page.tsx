'use client';

import { useEffect, useState, useCallback, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { productService } from '@/services/productService';
import { ProductSummary } from '@/types/product.types';
import { PageResponse } from '@/types/response.types';
import ProductCard from '@/components/ProductCard';

function SearchContent() {
    const searchParams = useSearchParams();
    const query = searchParams.get('q') || '';
    const categoryId = searchParams.get('category') || '';

    const [data, setData] = useState<PageResponse<ProductSummary> | null>(null);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);

    const loadData = useCallback(async () => {
        try {
            setIsLoading(true);

            const response = categoryId
                ? await productService.getByCategory(categoryId, page)
                : await productService.searchProducts(query, page);

            setData(response);
        } catch (error: unknown) {
            console.error('Search failed:', error);
        } finally {
            setIsLoading(false);
        }
    }, [query, categoryId, page]);

    useEffect(() => {
        void loadData();
    }, [loadData]);

    useEffect(() => {
        setPage(0);
    }, [query, categoryId]);

    return (
        <main className="min-h-screen bg-[#fafafa] pt-32 pb-20 px-8">
            <div className="max-w-[1400px] mx-auto">
                {/* Header Section */}
                <div className="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-6">
                    <div>
                        <h1 className="text-6xl font-black tracking-tighter text-black uppercase leading-[0.8]">
                            {categoryId ? 'Archive' : 'Search'}
                        </h1>

                        <p className="text-gray-400 text-sm mt-4 font-medium tracking-[0.3em] uppercase">
                            {categoryId ? `Category ID: ${categoryId}` : `Query: ${query}`}
                        </p>
                    </div>

                    <div className="bg-black text-white px-6 py-2 rounded-full text-[10px] font-black uppercase tracking-widest">
                        {data?.totalElements ?? 0} Objects found
                    </div>
                </div>

                {/* Grid Section */}
                {isLoading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10">
                        {Array.from({ length: 8 }, (_, i) => (
                            <div key={i} className="space-y-4">
                                <div className="h-80 bg-gray-200 animate-pulse rounded-[2rem]" />
                                <div className="h-4 w-2/3 bg-gray-200 animate-pulse rounded-full" />
                                <div className="h-4 w-1/3 bg-gray-200 animate-pulse rounded-full" />
                            </div>
                        ))}
                    </div>
                ) : data?.content.length ? (
                    <>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10">
                            {data.content.map((product) => (
                                <ProductCard
                                    key={product.productId}
                                    product={product}
                                />
                            ))}
                        </div>

                        {/* Pagination */}
                        <div className="mt-24 flex flex-col items-center gap-6">
                            <div className="flex items-center gap-4">
                                <PaginationButton
                                    direction="left"
                                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                                    disabled={page === 0}
                                />

                                <div className="flex items-center gap-2 px-6 py-3 bg-white border border-gray-100 rounded-full shadow-sm">
                                    <span className="text-xs font-black tracking-tighter text-black">
                                        {page + 1}
                                    </span>

                                    <span className="text-[10px] font-bold text-gray-300 uppercase">
                                        of
                                    </span>

                                    <span className="text-xs font-black tracking-tighter text-black">
                                        {data.totalPages}
                                    </span>
                                </div>

                                <PaginationButton
                                    direction="right"
                                    onClick={() => setPage((p) => p + 1)}
                                    disabled={page >= data.totalPages - 1}
                                />
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="py-40 flex flex-col items-center justify-center bg-white rounded-[4rem] border border-dashed border-gray-200">
                        <p className="text-sm font-black uppercase tracking-[0.4em] text-gray-300">
                            No results found
                        </p>
                    </div>
                )}
            </div>
        </main>
    );
}

interface PaginationButtonProps {
    direction: 'left' | 'right';
    onClick: () => void;
    disabled: boolean;
}

function PaginationButton({
                              direction,
                              onClick,
                              disabled,
                          }: PaginationButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            aria-label={direction === 'left' ? 'Previous page' : 'Next page'}
            className="w-14 h-14 flex items-center justify-center rounded-full bg-black text-white disabled:bg-gray-100 disabled:text-gray-400 transition-all hover:scale-110 active:scale-90 shadow-xl shadow-black/10"
        >
            {direction === 'left' ? '←' : '→'}
        </button>
    );
}

export default function SearchPage() {
    return (
        <Suspense
            fallback={
                <div className="h-screen flex items-center justify-center text-[10px] font-black uppercase tracking-[0.5em]">
                    Initializing...
                </div>
            }
        >
            <SearchContent />
        </Suspense>
    );
}