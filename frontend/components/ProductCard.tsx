'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ProductSummary } from "@/types/product.types";
import { cartService } from "@/services/cartService";

interface ProductCardProps {
    product: ProductSummary;
}

export default function ProductCard({ product }: ProductCardProps) {
    const [isAdding, setIsAdding] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);

    const hasDiscount = product.promotionValue !== null && product.promotionValue > 0;

    const discountedPrice = hasDiscount
        ? product.price * (1 - (product.promotionValue! / 100))
        : product.price;

    const handleAddToCart = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (isAdding) return;

        try {
            setIsAdding(true);
            await cartService.addToCart(product.productId.toString());

            setIsSuccess(true);
            setTimeout(() => setIsSuccess(false), 2000);
        } catch (error) {
            console.error("Failed to add to cart", error);
        } finally {
            setIsAdding(false);
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100 overflow-hidden group relative">
            <Link href={`/product/${product.productId}`}>

                <div className="relative h-48 w-full bg-gray-50 flex items-center justify-center overflow-hidden border-b border-gray-50">

                    {hasDiscount && (
                        <div className="absolute top-3 left-3 z-10 bg-red-600 text-white text-[9px] font-black uppercase tracking-widest px-2.5 py-1 rounded-md shadow-sm">
                            -{product.promotionValue}%
                        </div>
                    )}

                    <img
                        src={product.mainImageUrl || '/placeholder.png'}
                        alt={product.productName}
                        className="object-contain w-full h-full p-4 group-hover:scale-105 transition-transform duration-300"
                    />
                </div>

                <div className="p-4">
                    <h3 className="text-sm font-bold text-gray-800 truncate">
                        {product.productName}
                    </h3>

                    <div className="flex items-baseline gap-2 mt-2">
                        {hasDiscount ? (
                            <>
                                <span className="text-blue-600 font-black text-base">
                                    ${discountedPrice.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                </span>
                                <span className="text-xs text-gray-400 line-through font-medium">
                                    ${product.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                </span>
                            </>
                        ) : (
                            <span className="text-gray-900 font-black text-base">
                                ${product.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </span>
                        )}
                    </div>

                    <button
                        onClick={handleAddToCart}
                        disabled={isAdding}
                        className={`w-full mt-4 py-2.5 rounded-lg transition-colors text-xs font-bold uppercase tracking-wider
                            ${isSuccess
                            ? 'bg-emerald-600 text-white shadow-sm'
                            : hasDiscount
                                ? 'bg-red-50 text-red-600 hover:bg-red-600 hover:text-white'
                                : 'bg-gray-50 text-gray-700 hover:bg-blue-600 hover:text-white'
                        } ${isAdding ? 'opacity-50 cursor-wait' : ''}`}
                    >
                        {isAdding ? 'Adding...' : isSuccess ? 'Added ✓' : 'Add to Cart'}
                    </button>
                </div>
            </Link>
        </div>
    );
}