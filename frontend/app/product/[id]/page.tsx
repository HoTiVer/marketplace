"use client";

import { use, useEffect, useState } from "react";
import { productService } from "@/services/productService";
import { cartService } from "@/services/cartService";
import { wishlistService } from "@/services/wishlistService";
import { ProductTypes } from "@/types/product.types";
import Link from "next/link";

interface TimeLeft {
    days: number;
    hours: number;
    minutes: number;
    seconds: number;
}

export default function ProductPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);

    const [product, setProduct] = useState<ProductTypes | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [activeImage, setActiveImage] = useState<string>("");

    const [isAdding, setIsAdding] = useState(false);
    const [isWishlisting, setIsWishlisting] = useState(false);
    const [showCartSuccess, setShowCartSuccess] = useState(false);
    const [showWishlistSuccess, setShowWishlistSuccess] = useState(false);

    const [timeLeft, setTimeLeft] = useState<TimeLeft | null>(null);
    const [localEndTime, setLocalEndTime] = useState<string>("");

    useEffect(() => {
        async function loadProduct() {
            try {
                const data = await productService.getProductById(id);
                if (!data || ('message' in data)) {
                    setError(data?.message || "Product not found");
                    return;
                }
                setProduct(data);
                const mainImg = data.images?.find(img => img.isMain)?.url || data.images?.[0]?.url;
                setActiveImage(mainImg || "");
            } catch (err) {
                setError("Something went wrong");
            } finally {
                setLoading(false);
            }
        }
        loadProduct();
    }, [id]);

    useEffect(() => {
        if (!product?.promotion?.isPromotion || !product.promotion.promotionEndTime) {
            setTimeLeft(null);
            return;
        }

        const endTime = new Date(product.promotion.promotionEndTime);

        setLocalEndTime(
            endTime.toLocaleString(undefined, {
                dateStyle: "medium",
                timeStyle: "short",
            })
        );

        const calculateTimeLeft = () => {
            const now = new Date();
            const difference = endTime.getTime() - now.getTime();

            if (difference <= 0) {
                setTimeLeft(null);
                return false;
            }

            const days = Math.floor(difference / (1000 * 60 * 60 * 24));
            const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
            const minutes = Math.floor((difference / 1000 / 60) % 60);
            const seconds = Math.floor((difference / 1000) % 60);

            setTimeLeft({ days, hours, minutes, seconds });
            return true;
        };

        const isRunning = calculateTimeLeft();
        if (!isRunning) return;

        const timer = setInterval(calculateTimeLeft, 1000);

        return () => clearInterval(timer);
    }, [product]);

    const handleAddToCart = async () => {
        try {
            setIsAdding(true);
            await cartService.addToCart(id);
            setShowCartSuccess(true);
            setTimeout(() => setShowCartSuccess(false), 2500);
        } catch (err) {
            console.error("Cart error:", err);
        } finally {
            setIsAdding(false);
        }
    };

    const handleWishlist = async () => {
        try {
            setIsWishlisting(true);
            await wishlistService.addToWishlist(Number(id));
            setShowWishlistSuccess(true);
            setTimeout(() => setShowWishlistSuccess(false), 2500);
        } catch (err) {
            console.error("Wishlist error:", err);
        } finally {
            setIsWishlisting(false);
        }
    };

    if (loading) return (
        <div className="flex min-h-[80vh] items-center justify-center bg-gray-50">
            <div className="h-10 w-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    if (error || !product) return (
        <div className="min-h-[80vh] flex flex-col items-center justify-center p-6 text-center bg-gray-50">
            <div className="bg-white p-16 rounded-3xl shadow-xl shadow-gray-100 border border-gray-100">
                <h1 className="text-9xl font-black mb-4 tracking-tighter text-gray-900">404</h1>
                <p className="text-gray-500 mb-10 font-bold uppercase tracking-widest">{error || "Product unavailable"}</p>
                <Link href="/" className="px-12 py-5 bg-blue-600 text-white text-sm font-bold uppercase tracking-wider rounded-xl shadow-lg shadow-blue-200 hover:bg-blue-700 hover:shadow-blue-300 transition-all active:scale-[0.98]">
                    Back to catalog
                </Link>
            </div>
        </div>
    );

    const hasActivePromotion = !!(product.promotion?.isPromotion && typeof product.promotion?.promotionPrice === 'number');

    const isFree = hasActivePromotion && product.promotion!.promotionPrice === 0;

    const discountPercent = hasActivePromotion
        ? Math.round((1 - product.promotion!.promotionPrice / product.promotion!.defaultPrice) * 100)
        : 0;

    return (
        <main className="bg-gray-50 min-h-screen pt-8 p-4 md:p-8 pb-24 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-[1440px] mx-auto space-y-8">

                <div className="flex items-center gap-2 text-sm text-gray-500">
                    <Link href="/" className="hover:text-blue-600 transition-colors">Home</Link>
                    <span>/</span>
                    <span className="text-gray-900 font-medium">{product.categoryName || 'Product'}</span>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

                    <div className="lg:col-span-7 bg-white p-6 rounded-3xl border border-gray-100 shadow-sm flex flex-col gap-6">
                        <div className="aspect-[4/3] w-full relative overflow-hidden rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-center group">
                            {activeImage ? (
                                <img
                                    src={activeImage}
                                    alt={product.name}
                                    className="w-full h-full object-contain p-8 group-hover:scale-105 transition-transform duration-700 ease-out"
                                />
                            ) : (
                                <div className="text-gray-300 uppercase font-black tracking-widest text-xs">No image</div>
                            )}
                        </div>

                        {product.images?.length ? (
                            <div className="flex gap-4 overflow-x-auto pb-2 scrollbar-hide">
                                {product.images.map((img, idx) => (
                                    <button
                                        key={idx}
                                        onClick={() => setActiveImage(img.url)}
                                        className={`relative w-24 h-24 shrink-0 rounded-xl overflow-hidden border-2 transition-all duration-300 bg-gray-50 ${
                                            activeImage === img.url ? 'border-blue-600 scale-95 shadow-md shadow-blue-100' : 'border-gray-100 opacity-60 hover:opacity-100 hover:border-gray-200'
                                        }`}
                                    >
                                        <img src={img.url} alt="" className="w-full h-full object-cover p-1" />
                                    </button>
                                ))}
                            </div>
                        ) : null}
                    </div>

                    <div className="lg:col-span-5 space-y-8 lg:sticky lg:top-8">
                        <div className="bg-white p-8 md:p-10 rounded-3xl border border-gray-100 shadow-xl shadow-gray-100/50">
                            <div className="mb-8">
                                <div className="flex items-center gap-3 mb-4">
                                    <span className="px-3 py-1 bg-blue-50 text-blue-700 text-xs font-bold uppercase tracking-wider rounded-full border border-blue-100">
                                        {product.categoryName || 'Gadgets'}
                                    </span>
                                    {hasActivePromotion && (
                                        <span className={`px-3 py-1 text-white text-xs font-black uppercase tracking-wider rounded-full animate-pulse shadow-sm ${
                                            isFree
                                                ? 'bg-emerald-600 shadow-emerald-100'
                                                : 'bg-red-600 shadow-red-100'
                                        }`}>
                                            {isFree ? 'FREE OFFER' : `-${discountPercent}% OFF`}
                                        </span>
                                    )}
                                    <div className="flex items-center gap-1 text-yellow-500 text-sm font-bold">
                                        ★ ★ ★ ★ <span className="text-gray-400">☆ (4.0)</span>
                                    </div>
                                </div>

                                <h1 className="text-4xl md:text-5xl font-black tracking-tighter text-gray-900 mb-6 leading-tight">
                                    {product.name}
                                </h1>

                                {/* Блок цен с поддержкой скидки, 100% бесплатности и процента экономии */}
                                <div className="flex items-baseline gap-3 flex-wrap">
                                    {hasActivePromotion ? (
                                        <>
                                            {isFree ? (
                                                <span className="text-5xl font-black tracking-tighter text-emerald-600">
                                                    FREE
                                                </span>
                                            ) : (
                                                <span className="text-5xl font-black tracking-tighter text-red-600">
                                                    ${product.promotion!.promotionPrice.toLocaleString()}
                                                </span>
                                            )}
                                            <span className="text-lg text-gray-400 line-through font-medium">
                                                ${product.promotion!.defaultPrice.toLocaleString()}
                                            </span>
                                            <span className={`px-2.5 py-1 text-xs font-extrabold rounded-lg border ${
                                                isFree
                                                    ? 'bg-emerald-50 text-emerald-600 border-emerald-100'
                                                    : 'bg-red-50 text-red-600 border-red-100'
                                            }`}>
                                                {isFree ? '100% Off!' : `Save ${discountPercent}%`}
                                            </span>
                                        </>
                                    ) : (
                                        <span className="text-5xl font-black tracking-tighter text-blue-600">
                                            ${product.price.toLocaleString()}
                                        </span>
                                    )}
                                </div>

                                {/* Блок визуального таймера до конца скидки */}
                                {timeLeft && (
                                    <div className={`mt-6 p-4 border rounded-2xl space-y-3 ${
                                        isFree
                                            ? 'bg-emerald-50/60 border-emerald-100/70'
                                            : 'bg-red-50/60 border-red-100/70'
                                    }`}>
                                        <div className={`text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 ${
                                            isFree ? 'text-emerald-700' : 'text-red-700'
                                        }`}>
                                            <span className={`w-2 h-2 rounded-full animate-ping ${
                                                isFree ? 'bg-emerald-600' : 'bg-red-600'
                                            }`} />
                                            {isFree ? 'Limited Free Claim Time!' : 'Limited Time Offer!'}
                                        </div>
                                        <div className="flex gap-2 text-center">
                                            <div className={`bg-white px-3 py-2 rounded-xl border min-w-[55px] ${isFree ? 'border-emerald-100' : 'border-red-100'}`}>
                                                <div className={`text-lg font-black leading-none ${isFree ? 'text-emerald-600' : 'text-red-600'}`}>{timeLeft.days}</div>
                                                <div className="text-[9px] uppercase text-gray-400 font-bold mt-1">Days</div>
                                            </div>
                                            <div className={`bg-white px-3 py-2 rounded-xl border min-w-[55px] ${isFree ? 'border-emerald-100' : 'border-red-100'}`}>
                                                <div className={`text-lg font-black leading-none ${isFree ? 'text-emerald-600' : 'text-red-600'}`}>
                                                    {String(timeLeft.hours).padStart(2, "0")}
                                                </div>
                                                <div className="text-[9px] uppercase text-gray-400 font-bold mt-1">Hours</div>
                                            </div>
                                            <div className={`bg-white px-3 py-2 rounded-xl border min-w-[55px] ${isFree ? 'border-emerald-100' : 'border-red-100'}`}>
                                                <div className={`text-lg font-black leading-none ${isFree ? 'text-emerald-600' : 'text-red-600'}`}>
                                                    {String(timeLeft.minutes).padStart(2, "0")}
                                                </div>
                                                <div className="text-[9px] uppercase text-gray-400 font-bold mt-1">Min</div>
                                            </div>
                                            <div className={`bg-white px-3 py-2 rounded-xl border min-w-[55px] ${isFree ? 'border-emerald-100' : 'border-red-100'}`}>
                                                <div className={`text-lg font-black leading-none ${isFree ? 'text-emerald-600' : 'text-red-600'}`}>
                                                    {String(timeLeft.seconds).padStart(2, "0")}
                                                </div>
                                                <div className="text-[9px] uppercase text-gray-400 font-bold mt-1">Sec</div>
                                            </div>
                                        </div>
                                        <div className="text-[11px] text-gray-500 font-medium">
                                            Ends: <span className="font-bold text-gray-700">{localEndTime}</span>
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="flex flex-col gap-4">
                                <button
                                    onClick={handleAddToCart}
                                    disabled={isAdding || showCartSuccess}
                                    className={`w-full py-5 text-base font-bold text-white rounded-xl shadow-lg transition-all active:scale-[0.98] flex items-center justify-center gap-4 
                                        ${showCartSuccess
                                        ? 'bg-emerald-600 shadow-emerald-200'
                                        : isFree
                                            ? 'bg-emerald-600 shadow-emerald-200 hover:bg-emerald-700 hover:shadow-emerald-300'
                                            : hasActivePromotion
                                                ? 'bg-red-600 shadow-red-200 hover:bg-red-700 hover:shadow-red-300'
                                                : 'bg-blue-600 shadow-blue-200 hover:bg-blue-700 hover:shadow-blue-300'
                                    } ${isAdding ? 'opacity-70 cursor-wait' : ''}`}
                                >
                                    {isAdding && <span className="h-5 w-5 border-2 border-white/20 border-t-white rounded-full animate-spin" />}
                                    {showCartSuccess
                                        ? '✓ Added to Cart'
                                        : isAdding
                                            ? 'Processing...'
                                            : isFree
                                                ? 'Get for Free'
                                                : 'Add to Cart'}
                                </button>

                                <div className="grid grid-cols-2 gap-4">
                                    <button
                                        onClick={handleWishlist}
                                        disabled={isWishlisting || showWishlistSuccess}
                                        className={`py-4 text-sm font-bold rounded-xl border-2 transition-all flex items-center justify-center gap-2.5 active:scale-[0.98]
                                            ${showWishlistSuccess
                                            ? 'bg-red-50 border-red-100 text-red-600 shadow-inner'
                                            : 'bg-white border-gray-200 text-gray-700 hover:border-red-300 hover:bg-red-50 hover:text-red-600'
                                        }`}
                                    >
                                        <span className="text-base">{showWishlistSuccess ? '❤️' : '♡'}</span>
                                        {showWishlistSuccess ? 'Saved' : 'Wishlist'}
                                    </button>
                                    <Link
                                        href={`/product/${id}/reviews`}
                                        className="py-4 bg-white text-gray-700 text-sm font-bold rounded-xl border-2 border-gray-200 hover:border-gray-300 hover:bg-gray-50 transition-all text-center flex items-center justify-center gap-2.5"
                                    >
                                        <span className="text-base">💬</span> Reviews
                                    </Link>
                                </div>
                            </div>
                        </div>

                        <div className="bg-white p-6 rounded-3xl border border-gray-100 flex items-center justify-between group shadow-sm">
                            <div className="flex items-center gap-4">
                                <div className="w-14 h-14 bg-gray-900 rounded-full flex items-center justify-center text-white font-black text-xl shadow-inner border-4 border-gray-100">
                                    {product.sellerDisplayName?.charAt(0)}
                                </div>
                                <div className="flex flex-col">
                                    <span className="text-xs font-bold text-gray-400 uppercase tracking-wider">Sold by</span>
                                    <span className="text-lg font-extrabold text-gray-900 tracking-tight group-hover:text-blue-600 transition-colors">
                                        {product.sellerDisplayName}
                                    </span>
                                    <span className="text-xs text-gray-400 font-medium">@{product.sellerUsername}</span>
                                </div>
                            </div>
                            {product.sellerUsername && (
                                <Link
                                    href={`/seller/${product.sellerUsername}`}
                                    className="px-5 py-3 bg-gray-50 border border-gray-100 text-xs font-bold text-gray-700 rounded-lg hover:bg-gray-900 hover:text-white hover:border-gray-900 transition-all active:scale-95"
                                >
                                    Store Profile
                                </Link>
                            )}
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 pt-8 border-t border-gray-100">
                    <div className="md:col-span-1 bg-white p-8 rounded-3xl border border-gray-100 shadow-sm space-y-8">
                        <h3 className="text-xl font-black text-gray-900 tracking-tight border-b border-gray-100 pb-5">
                            Technical Specs
                        </h3>
                        <div className="space-y-5">
                            {product.characteristic && Object.keys(product.characteristic).length ? (
                                Object.entries(product.characteristic).map(([key, value]) => (
                                    <div key={key} className="flex justify-between items-center gap-4 pb-3 border-b border-gray-50 last:border-0 last:pb-0">
                                        <span className="text-xs font-bold uppercase tracking-wider text-gray-400">{key}</span>
                                        <span className="text-sm font-semibold text-gray-900 text-right">{value}</span>
                                    </div>
                                ))
                            ) : (
                                <p className="text-sm text-gray-400 font-medium italic py-4">Data unavailable</p>
                            )}
                        </div>
                    </div>

                    <div className="md:col-span-2 bg-white p-8 rounded-3xl border border-gray-100 shadow-sm space-y-6">
                        <h3 className="text-xl font-black text-gray-900 tracking-tight">
                            Product Description
                        </h3>
                        <div className="prose prose-blue max-w-none text-gray-600 leading-relaxed font-medium italic">
                            <p>&#34;{product.description}&#34;</p>
                        </div>
                    </div>
                </div>

            </div>
        </main>
    );
}