'use client';

import { useEffect, useState } from 'react';
import { wishlistService } from "@/services/wishlistService";
import { cartService } from "@/services/cartService";
import Link from 'next/link';
import { ProductSummary } from "@/types/product.types";

export default function WishlistPage() {
    const [items, setItems] = useState<ProductSummary[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadWishlist = async () => {
            try {
                const data = await wishlistService.getWishlist();
                setItems(data);
            } catch (error) {
                console.error("Failed to load wishlist", error);
            } finally {
                setLoading(false);
            }
        };
        loadWishlist();
    }, []);

    const handleDelete = async (productId: number) => {
        const previous = [...items];
        setItems(current => current.filter(item => item.productId !== productId));
        try {
            await wishlistService.removeFromWishlist(productId);
        } catch (error) {
            setItems(previous);
        }
    };

    const handleMoveToCart = async (productId: number) => {
        try {
            await cartService.addToCart(productId.toString());
            handleDelete(productId);
        } catch (error) {
            console.error("Move to cart failed");
        }
    };

    if (loading) return (
        <div className="flex min-h-[80vh] items-center justify-center bg-gray-50">
            <div className="h-10 w-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    return (
        <main className="bg-gray-50 min-h-screen pt-24 pb-20 px-4 md:px-8 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-4xl mx-auto space-y-10">

                <header className="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-gray-100 pb-10">
                    <div>
                        <h1 className="text-5xl font-black tracking-tighter text-gray-900 leading-none uppercase italic">
                            Saved <span className="text-red-500">Items</span>
                        </h1>
                        <p className="text-gray-400 font-bold uppercase tracking-widest text-[10px] mt-4 ml-1">
                            {items.length} products in your wishlist
                        </p>
                    </div>
                    <Link href="/" className="inline-flex items-center gap-2 text-xs font-black uppercase tracking-widest text-gray-400 hover:text-blue-600 transition-colors">
                        ← Back to shop
                    </Link>
                </header>

                {items.length > 0 ? (
                    <div className="bg-white rounded-[3rem] border border-gray-100 shadow-xl shadow-gray-200/40 overflow-hidden">
                        {items.map((item) => (
                            <div key={item.productId} className="group flex flex-col sm:flex-row items-center justify-between p-8 border-b border-gray-50 last:border-0 hover:bg-gray-50/50 transition-all duration-300">
                                <div className="flex items-center gap-8 w-full sm:w-auto">
                                    <div className="w-28 h-28 bg-white rounded-3xl overflow-hidden flex-shrink-0 p-4 shadow-inner border border-gray-50 relative group-hover:rotate-2 transition-transform duration-500">
                                        <img
                                            src={item.mainImageUrl || '/placeholder.png'}
                                            alt=""
                                            className="w-full h-full object-contain"
                                        />
                                        <div className="absolute top-2 right-2 text-xs">❤️</div>
                                    </div>

                                    <div className="flex flex-col gap-2">
                                        <Link href={`/product/${item.productId}`} className="text-lg font-black tracking-tighter text-gray-900 hover:text-blue-600 transition-colors uppercase italic leading-tight">
                                            {item.productName}
                                        </Link>
                                        <div className="flex items-center gap-3">
                                            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                                            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">
                                                Ready for shipping
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-8 mt-8 sm:mt-0 w-full sm:w-auto justify-between sm:justify-end">
                                    <p className="text-3xl font-black tracking-tighter text-gray-900 italic">
                                        ${item.price.toLocaleString()}
                                    </p>

                                    <div className="flex items-center gap-3">
                                        <button
                                            onClick={() => handleMoveToCart(item.productId)}
                                            className="px-8 py-4 bg-blue-600 text-white text-[10px] font-black uppercase tracking-widest rounded-2xl hover:bg-blue-700 shadow-xl shadow-blue-100 transition-all active:scale-95 flex items-center gap-3"
                                        >
                                            <span>🛒</span> Move to bag
                                        </button>
                                        <button
                                            onClick={() => handleDelete(item.productId)}
                                            className="w-12 h-12 flex items-center justify-center text-gray-300 hover:text-red-500 hover:bg-red-50 border border-gray-100 rounded-2xl transition-all active:scale-90"
                                        >
                                            ✕
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="py-32 text-center bg-white rounded-[3rem] border-2 border-dashed border-gray-100 flex flex-col items-center gap-6 shadow-sm">
                        <div className="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center text-5xl grayscale opacity-50">
                            🖤
                        </div>
                        <div>
                            <p className="text-lg font-black text-gray-900 uppercase italic tracking-tighter">Your Heart is Empty</p>
                            <p className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.3em] mt-2">Save things you love for later</p>
                        </div>
                        <Link href="/" className="px-10 py-5 bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest rounded-2xl hover:bg-blue-600 transition-all shadow-xl active:scale-95">
                            Explore items
                        </Link>
                    </div>
                )}
            </div>
        </main>
    );
}