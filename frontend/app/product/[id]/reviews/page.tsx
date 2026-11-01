"use client";

import { use, useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { reviewService } from "@/services/reviewService";
import { ProductReviewResponse } from "@/types/review.types";

export default function ReviewsPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
    const [data, setData] = useState<ProductReviewResponse | null>(null);
    const [loading, setLoading] = useState(true);

    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const loadReviews = useCallback(async () => {
        try {
            const result = await reviewService.getProductReviews(id);
            setData(result);
        } catch (err) {
            console.error("Failed to load reviews");
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        loadReviews();
    }, [loadReviews]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!comment.trim()) return;
        setIsSubmitting(true);
        try {
            await reviewService.createReview(id, { rating, comment });
            setComment("");
            setRating(5);
            await loadReviews();
        } catch (err) {
            console.error("Submit failed");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return (
        <div className="flex min-h-[80vh] items-center justify-center bg-gray-50">
            <div className="h-10 w-10 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin"></div>
        </div>
    );

    return (
        <main className="bg-gray-50 min-h-screen pt-24 p-4 md:p-8 pb-24 selection:bg-blue-100 selection:text-blue-900">
            <div className="max-w-[1200px] mx-auto space-y-8">

                <header className="space-y-4">
                    <Link href={`/product/${id}`} className="inline-flex items-center gap-2 text-sm font-bold text-gray-400 hover:text-blue-600 transition-colors">
                        <span className="text-lg">←</span> Back to product
                    </Link>

                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                        <div>
                            <p className="text-blue-600 font-bold uppercase tracking-widest text-[10px] mb-2">
                                Customer Reviews for
                            </p>
                            <Link href={`/product/${data?.productId || id}`} className="group">
                                <h1 className="text-5xl font-black tracking-tighter text-gray-900 leading-none group-hover:text-blue-600 transition-colors">
                                    {data?.productName || "Product Reviews"}
                                </h1>
                            </Link>
                            <p className="text-gray-400 font-bold uppercase tracking-widest text-[10px] mt-4">
                                Based on {data?.productReviews?.length || 0} opinions
                            </p>
                        </div>

                        {data?.rating !== null && data?.rating !== undefined && (
                            <div className="bg-white p-6 rounded-3xl border border-gray-100 shadow-xl shadow-gray-200/50 flex items-center gap-4">
                                <span className="text-4xl font-black tracking-tighter text-gray-900">
                                    {data.rating.toFixed(1)}
                                </span>
                                <div className="flex flex-col">
                                    <div className="flex text-yellow-500 text-lg">
                                        {"★".repeat(Math.round(data.rating))}{"☆".repeat(5 - Math.round(data.rating))}
                                    </div>
                                    <span className="text-[10px] font-black uppercase tracking-wider text-gray-400">Average Rating</span>
                                </div>
                            </div>
                        )}
                    </div>
                </header>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

                    <div className="lg:col-span-4 lg:sticky lg:top-24">
                        <form onSubmit={handleSubmit} className="bg-white p-8 rounded-3xl border border-gray-100 shadow-xl shadow-gray-200/40 space-y-6">
                            <div className="flex items-center gap-3">
                                <span className="text-2xl">✍️</span>
                                <h3 className="text-lg font-black text-gray-900 tracking-tight">Share Experience</h3>
                            </div>

                            <div className="space-y-3">
                                <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 block ml-1">Rating</label>
                                <div className="flex justify-between gap-2">
                                    {[1, 2, 3, 4, 5].map((num) => (
                                        <button
                                            key={num} type="button"
                                            onClick={() => setRating(num)}
                                            className={`flex-1 py-3 rounded-xl font-black transition-all active:scale-95 ${
                                                rating >= num
                                                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-200'
                                                    : 'bg-gray-50 text-gray-400 hover:bg-gray-100'
                                            }`}
                                        >
                                            {num}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="space-y-3">
                                <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 block ml-1">Message</label>
                                <textarea
                                    value={comment}
                                    onChange={(e) => setComment(e.target.value)}
                                    className="w-full bg-gray-50 rounded-2xl p-4 text-sm font-bold text-gray-900 outline-none focus:bg-white focus:ring-4 ring-blue-500/10 border border-transparent focus:border-blue-500 min-h-[140px] resize-none transition-all placeholder:text-gray-300"
                                    placeholder="What do you think about the product?"
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={isSubmitting || !comment.trim()}
                                className="w-full bg-gray-900 text-white py-4 rounded-xl text-xs font-black uppercase tracking-widest hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-200 transition-all disabled:opacity-30 active:scale-[0.98]"
                            >
                                {isSubmitting ? 'Sending...' : 'Post Review'}
                            </button>
                        </form>
                    </div>

                    <div className="lg:col-span-8 space-y-4">
                        {data?.productReviews?.length ? (
                            data.productReviews.map((rev) => (
                                <div key={rev.reviewId} className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
                                    <div className="flex justify-between items-center mb-6">
                                        <div className="flex items-center gap-4">
                                            <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 font-black text-sm border border-blue-100">
                                                {rev.commentatorName?.charAt(0) || "U"}
                                            </div>
                                            <div>
                                                <p className="text-sm font-black text-gray-900 tracking-tight">{rev.commentatorName}</p>
                                                <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">
                                                    {new Date(rev.updatedAt).toLocaleDateString(undefined, {
                                                        year: 'numeric',
                                                        month: 'long',
                                                        day: 'numeric'
                                                    })}
                                                </p>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-gray-900 text-white rounded-lg shadow-sm">
                                            <span className="text-xs">⭐</span>
                                            <span className="text-xs font-black">{rev.rating}.0</span>
                                        </div>
                                    </div>
                                    <div className="prose prose-blue max-w-none">
                                        <p className="text-gray-600 font-medium italic leading-relaxed">
                                            {rev.comment || 'The user left no comments, just a rating.'}
                                        </p>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="py-24 text-center bg-white rounded-3xl border-2 border-dashed border-gray-100 flex flex-col items-center gap-4">
                                <span className="text-5xl">📦</span>
                                <div>
                                    <p className="text-lg font-black text-gray-900">No reviews yet</p>
                                    <p className="text-sm font-bold text-gray-400">Be the first to share your experience!</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </main>
    );
}